package com.example.ui.screens

import com.example.ui.screens.futureself.VoiceCloningSettingsSection
import android.content.Context
import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.GraphicEq
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.icons.filled.AudioFile
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MusicNote
import com.example.util.RingtoneStorageManager
import androidx.compose.material.icons.filled.PhoneInTalk
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.TextButton
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.core.content.ContextCompat
import android.Manifest
import android.content.pm.PackageManager
import com.example.speech.AudioRecorderHelper
import com.example.speech.VoiceCloningService
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.futureself.FutureSelfVoiceSettings
import com.example.data.model.futureself.VoiceGender
import com.example.data.model.futureself.VoiceLanguage
import com.example.data.model.futureself.VoiceTone
import com.example.data.repository.FutureSelfSettingsRepository
import com.example.notification.FutureSelfCallActivity
import com.example.speech.FutureSelfSpeechManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FutureSelfSettingsScreen(
    onNavigateBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val repository = remember { FutureSelfSettingsRepository.getInstance(context) }
    val settings by repository.settingsFlow.collectAsStateWithLifecycle()

    var isEnabled by remember(settings) { mutableStateOf(settings.isEnabled) }
    var selectedGender by remember(settings) { mutableStateOf(settings.voiceGender) }
    var selectedTone by remember(settings) { mutableStateOf(settings.tone) }
    var selectedLanguage by remember(settings) { mutableStateOf(settings.language) }
    var speechSpeed by remember(settings) { mutableFloatStateOf(settings.speechSpeed) }
    var volume by remember(settings) { mutableFloatStateOf(settings.volume) }
    var vibrationEnabled by remember(settings) { mutableStateOf(settings.vibrationEnabled) }
    var selectedRingtone by remember(settings) { mutableStateOf(settings.ringtonePreset) }
    var customQuote by remember(settings) { mutableStateOf(settings.customFutureSelfQuote) }

    val speechManager = remember { FutureSelfSpeechManager.getInstance(context) }

    fun save(updated: FutureSelfVoiceSettings) {
        repository.updateSettings(updated)
    }

    val audioPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            val result = RingtoneStorageManager.saveCustomRingtone(context, uri)
            save(
                settings.copy(
                    customMaxDelayAudioUri = result.storedUriOrPath,
                    customMaxDelayAudioName = result.displayName
                )
            )
        }
    }

    val ringtonePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            val result = RingtoneStorageManager.saveCustomRingtone(context, uri)
            selectedRingtone = "CUSTOM"
            save(
                settings.copy(
                    ringtonePreset = "CUSTOM",
                    customRingtoneUri = result.storedUriOrPath,
                    customRingtoneName = result.displayName
                )
            )
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF030712),
                        Color(0xFF0F172A),
                        Color(0xFF02040A)
                    )
                )
            )
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // App Bar
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp, bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier
                            .size(40.dp)
                            .background(Color(0xFF1E293B), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = "Future Self Calling",
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Temporal Discipline Alert System",
                            color = Color(0xFF94A3B8),
                            fontSize = 12.sp
                        )
                    }
                }
            }

            // Master Enable Toggle Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                    border = BorderStroke(1.dp, if (isEnabled) Color(0xFF00F0FF).copy(alpha = 0.5f) else Color(0xFF334155))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isEnabled) Color(0xFF00F0FF).copy(alpha = 0.15f) else Color(0xFF1E293B),
                                modifier = Modifier.size(48.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.PhoneInTalk,
                                        contentDescription = null,
                                        tint = if (isEnabled) Color(0xFF00F0FF) else Color(0xFF64748B),
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column {
                                Text(
                                    text = "Enable Future Self Calls",
                                    color = Color.White,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = if (isEnabled) "Active for all scheduled tasks" else "Disabled (standard alerts)",
                                    color = if (isEnabled) Color(0xFF10B981) else Color(0xFF94A3B8),
                                    fontSize = 12.sp
                                )
                            }
                        }

                        Switch(
                            checked = isEnabled,
                            onCheckedChange = { checked ->
                                isEnabled = checked
                                save(settings.copy(isEnabled = checked))
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color(0xFF00F0FF),
                                checkedTrackColor = Color(0xFF0284C7).copy(alpha = 0.5f)
                            )
                        )
                    }
                }
            }

            // Real AI Voice Cloning Section (Dynamic Future Self)
            item {
                SettingsSectionContainer(title = "AI VOICE CLONING (DYNAMIC FUTURE SELF)") {
                    VoiceCloningSettingsSection(
                        settings = settings,
                        onSaveSettings = { updated -> save(updated) },
                        speechManager = speechManager
                    )
                }
            }

            // Voice Type (Gender)
            item {
                SettingsSectionContainer(title = "VOICE GENDER") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        VoiceGender.values().forEach { gender ->
                            val isSelected = selectedGender == gender
                            Button(
                                onClick = {
                                    selectedGender = gender
                                    save(settings.copy(voiceGender = gender))
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isSelected) Color(0xFF0284C7) else Color(0xFF1E293B)
                                ),
                                border = BorderStroke(1.dp, if (isSelected) Color(0xFF38BDF8) else Color(0xFF334155))
                            ) {
                                Text(
                                    text = gender.displayName,
                                    color = if (isSelected) Color.White else Color(0xFF94A3B8),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            // Tone Selection
            item {
                SettingsSectionContainer(title = "VOICE TONE") {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        VoiceTone.values().forEach { tone ->
                            val isSelected = selectedTone == tone
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) Color(0xFF1E293B) else Color(0xFF0B132B),
                                border = BorderStroke(1.dp, if (isSelected) Color(0xFF00F0FF) else Color(0xFF1E293B)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        selectedTone = tone
                                        save(settings.copy(tone = tone))
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text(
                                            text = tone.displayName,
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp
                                        )
                                        Text(
                                            text = tone.description,
                                            color = Color(0xFF94A3B8),
                                            fontSize = 12.sp
                                        )
                                    }
                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            tint = Color(0xFF00F0FF),
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Language Selection
            item {
                SettingsSectionContainer(title = "LANGUAGE") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        VoiceLanguage.values().forEach { lang ->
                            val isSelected = selectedLanguage == lang
                            Button(
                                onClick = {
                                    selectedLanguage = lang
                                    save(settings.copy(language = lang))
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isSelected) Color(0xFF0284C7) else Color(0xFF1E293B)
                                ),
                                border = BorderStroke(1.dp, if (isSelected) Color(0xFF38BDF8) else Color(0xFF334155))
                            ) {
                                Text(
                                    text = lang.displayName,
                                    color = if (isSelected) Color.White else Color(0xFF94A3B8),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            }

            // Speech Speed & Volume Sliders
            item {
                SettingsSectionContainer(title = "AUDIO & SPEED DYNAMICS") {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        // Speed Slider
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Speed,
                                        contentDescription = null,
                                        tint = Color(0xFF38BDF8),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(text = "Speech Speed", color = Color.White, fontSize = 13.sp)
                                }
                                Text(
                                    text = String.format(java.util.Locale.US, "%.1fx", speechSpeed),
                                    color = Color(0xFF38BDF8),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
                            Slider(
                                value = speechSpeed,
                                onValueChange = {
                                    speechSpeed = it
                                    save(settings.copy(speechSpeed = it))
                                },
                                valueRange = 0.7f..1.5f,
                                steps = 7,
                                colors = SliderDefaults.colors(
                                    thumbColor = Color(0xFF38BDF8),
                                    activeTrackColor = Color(0xFF0284C7)
                                )
                            )
                        }

                        // Volume Slider
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.VolumeUp,
                                        contentDescription = null,
                                        tint = Color(0xFF10B981),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(text = "Volume", color = Color.White, fontSize = 13.sp)
                                }
                                Text(
                                    text = "${(volume * 100).toInt()}%",
                                    color = Color(0xFF10B981),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
                            Slider(
                                value = volume,
                                onValueChange = {
                                    volume = it
                                    save(settings.copy(volume = it))
                                },
                                valueRange = 0.1f..1.0f,
                                steps = 9,
                                colors = SliderDefaults.colors(
                                    thumbColor = Color(0xFF10B981),
                                    activeTrackColor = Color(0xFF059669)
                                )
                            )
                        }

                        // Vibration Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Vibration,
                                    contentDescription = null,
                                    tint = Color(0xFFF59E0B),
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = "Haptic Vibration Pulse", color = Color.White, fontSize = 14.sp)
                            }
                            Switch(
                                checked = vibrationEnabled,
                                onCheckedChange = { checked ->
                                    vibrationEnabled = checked
                                    save(settings.copy(vibrationEnabled = checked))
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color(0xFFF59E0B),
                                    checkedTrackColor = Color(0xFFB45309).copy(alpha = 0.5f)
                                )
                            )
                        }
                    }
                }
            }

            // Ringtone Preset Selection
            item {
                SettingsSectionContainer(title = "TACTICAL RINGTONE (CALL ALERT)") {
                    val ringtones = listOf(
                        "CYBER_SIREN" to "Cyber Siren (Futuristic Pulse)",
                        "APEX_HORNS" to "Apex Horns (Cinematic War Horn)",
                        "ZEN_CHIME" to "Zen Chime (Deep Focus)",
                        "BELL" to "Classic Bell (Resonant)",
                        "MILITARY" to "Military Reveille (Urgent)",
                        "TICK_TOCK" to "Tick Tock (Time Running Out)"
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Select preset tactical siren or choose your own custom ringtone from device storage.",
                            color = Color(0xFF94A3B8),
                            fontSize = 12.sp
                        )

                        // 1. Custom Ringtone from Phone Storage
                        val isCustomRingtone = selectedRingtone == "CUSTOM" || settings.ringtonePreset == "CUSTOM"
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isCustomRingtone) Color(0xFF1E293B) else Color(0xFF0B132B),
                            border = BorderStroke(1.dp, if (isCustomRingtone) Color(0xFF10B981) else Color(0xFF1E293B)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    if (settings.customRingtoneUri.isNotBlank()) {
                                        selectedRingtone = "CUSTOM"
                                        save(settings.copy(ringtonePreset = "CUSTOM"))
                                    } else {
                                        ringtonePickerLauncher.launch("audio/*")
                                    }
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                    Icon(
                                        imageVector = Icons.Default.MusicNote,
                                        contentDescription = null,
                                        tint = if (isCustomRingtone) Color(0xFF10B981) else Color(0xFF64748B),
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = "Custom Ringtone (From Device)",
                                            color = if (isCustomRingtone) Color.White else Color(0xFFCBD5E1),
                                            fontSize = 13.sp,
                                            fontWeight = if (isCustomRingtone) FontWeight.Bold else FontWeight.Medium
                                        )
                                        Text(
                                            text = if (settings.customRingtoneName.isNotBlank()) settings.customRingtoneName else "No file selected (Tap to choose from phone)",
                                            color = if (isCustomRingtone) Color(0xFF10B981) else Color(0xFF64748B),
                                            fontSize = 11.sp
                                        )
                                    }
                                }
                                if (isCustomRingtone) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = Color(0xFF10B981),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }

                        Button(
                            onClick = { ringtonePickerLauncher.launch("audio/*") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (settings.customRingtoneUri.isNotBlank()) Color(0xFF334155) else Color(0xFF0D9488)
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.AudioFile,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (settings.customRingtoneUri.isNotBlank()) "CHANGE CUSTOM RINGTONE FILE" else "CHOOSE CUSTOM RINGTONE FROM PHONE STORAGE",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        HorizontalDivider(color = Color(0xFF1E293B), thickness = 1.dp, modifier = Modifier.padding(vertical = 4.dp))

                        Text(
                            text = "OR CHOOSE FROM TACTICAL PRESETS:",
                            color = Color(0xFF64748B),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )

                        // 2. Preset Tactical Ringtones
                        ringtones.forEach { (key, label) ->
                            val isSelected = selectedRingtone == key && !isCustomRingtone
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) Color(0xFF1E293B) else Color(0xFF0B132B),
                                border = BorderStroke(1.dp, if (isSelected) Color(0xFF38BDF8) else Color(0xFF1E293B)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        selectedRingtone = key
                                        save(settings.copy(ringtonePreset = key))
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.MusicNote,
                                            contentDescription = null,
                                            tint = if (isSelected) Color(0xFF38BDF8) else Color(0xFF64748B),
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text(
                                            text = label,
                                            color = if (isSelected) Color.White else Color(0xFF94A3B8),
                                            fontSize = 13.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                        )
                                    }
                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            tint = Color(0xFF38BDF8),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Maximum Delay Limit & Automatic Discipline Section
            item {
                SettingsSectionContainer(title = "MAXIMUM DELAY & AUTOMATIC ENFORCEMENT") {
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        Text(
                            text = "When a task reaches maximum delay, voice directives begin playing automatically by itself without waiting for user action.",
                            color = Color(0xFF94A3B8),
                            fontSize = 12.sp
                        )

                        Text(
                            text = "Max Allowed Delay: ${settings.maxAllowedDelayMinutes} Minutes",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(30, 45, 60, 90).forEach { limit ->
                                val selected = settings.maxAllowedDelayMinutes == limit
                                Surface(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable {
                                            save(settings.copy(maxAllowedDelayMinutes = limit))
                                        },
                                    color = if (selected) Color(0xFF0284C7) else Color(0xFF1E293B),
                                    shape = RoundedCornerShape(10.dp),
                                    border = BorderStroke(1.dp, if (selected) Color(0xFF38BDF8) else Color(0xFF334155))
                                ) {
                                    Box(
                                        modifier = Modifier.padding(vertical = 10.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "$limit m",
                                            color = if (selected) Color.White else Color(0xFFCBD5E1),
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }

                        HorizontalDivider(color = Color(0xFF1E293B), thickness = 1.dp)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Auto-Play on Max Delay",
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Force voice playback immediately when max delay is hit",
                                    color = Color(0xFF94A3B8),
                                    fontSize = 11.sp
                                )
                            }
                            Switch(
                                checked = settings.autoStartOnMaxDelay,
                                onCheckedChange = {
                                    save(settings.copy(autoStartOnMaxDelay = it))
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = Color(0xFF10B981),
                                    uncheckedThumbColor = Color(0xFF94A3B8),
                                    uncheckedTrackColor = Color(0xFF1E293B)
                                )
                            )
                        }

                        HorizontalDivider(color = Color(0xFF1E293B), thickness = 1.dp)

                        Text(
                            text = "Unanswered Call Timeout: ${settings.callTimeoutSeconds}s (Auto-Ignition)",
                            color = Color(0xFFCBD5E1),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(20, 30, 40, 60).forEach { sec ->
                                val selected = settings.callTimeoutSeconds == sec
                                Surface(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable {
                                            save(settings.copy(callTimeoutSeconds = sec))
                                        },
                                    color = if (selected) Color(0xFF0284C7) else Color(0xFF1E293B),
                                    shape = RoundedCornerShape(10.dp),
                                    border = BorderStroke(1.dp, if (selected) Color(0xFF38BDF8) else Color(0xFF334155))
                                ) {
                                    Box(
                                        modifier = Modifier.padding(vertical = 8.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "${sec}s",
                                            color = if (selected) Color.White else Color(0xFFCBD5E1),
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }

                        HorizontalDivider(color = Color(0xFF1E293B), thickness = 1.dp)

                        Text(
                            text = "CUSTOM AUDIO CLIP (MAX DELAY HIT)",
                            color = Color(0xFFFCD34D),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )

                        Text(
                            text = "Pick any audio file from your device storage to play when max delay is reached. If unselected, offline Future Self TTS directive plays.",
                            color = Color(0xFF94A3B8),
                            fontSize = 12.sp
                        )

                        if (settings.customMaxDelayAudioUri.isNotBlank()) {
                            Surface(
                                color = Color(0xFF1E293B),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.5f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Active Custom Audio:",
                                            color = Color(0xFF94A3B8),
                                            fontSize = 11.sp
                                        )
                                        Text(
                                            text = settings.customMaxDelayAudioName.ifBlank { "Custom Audio File" },
                                            color = Color(0xFF10B981),
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    IconButton(
                                        onClick = {
                                            save(
                                                settings.copy(
                                                    customMaxDelayAudioUri = "",
                                                    customMaxDelayAudioName = ""
                                                )
                                            )
                                        }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Remove custom audio",
                                            tint = Color(0xFFEF4444)
                                        )
                                    }
                                }
                            }
                        }

                        Button(
                            onClick = { audioPickerLauncher.launch("audio/*") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (settings.customMaxDelayAudioUri.isNotBlank()) Color(0xFF334155) else Color(0xFFD97706)
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.AudioFile,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (settings.customMaxDelayAudioUri.isNotBlank()) "CHANGE CUSTOM AUDIO FILE" else "SELECT CUSTOM AUDIO FILE FROM DEVICE",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Custom Message Override
            item {
                SettingsSectionContainer(title = "CUSTOM FUTURE SELF MESSAGE") {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Leave empty to use automatic subject-specific quotes (Physics, Chemistry, Workout, etc.)",
                            color = Color(0xFF94A3B8),
                            fontSize = 12.sp
                        )
                        OutlinedTextField(
                            value = customQuote,
                            onValueChange = {
                                customQuote = it
                                save(settings.copy(customFutureSelfQuote = it))
                            },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = {
                                Text("e.g. You promised yourself top 100 rank. Get to work.", color = Color(0xFF475569))
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF38BDF8),
                                unfocusedBorderColor = Color(0xFF334155),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            )
                        )
                    }
                }
            }

            // Live Simulation / Test Buttons
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(
                        onClick = {
                            val testIntent = Intent(context, FutureSelfCallActivity::class.java).apply {
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                                putExtra(FutureSelfCallActivity.EXTRA_TASK_ID, 9999L)
                                putExtra(FutureSelfCallActivity.EXTRA_TASK_TITLE, "Physics Electrostatics")
                                putExtra(FutureSelfCallActivity.EXTRA_TASK_SUBJECT, "Physics")
                                putExtra(FutureSelfCallActivity.EXTRA_START_TIME, "05:00 AM")
                                putExtra(FutureSelfCallActivity.EXTRA_END_TIME, "06:30 AM")
                                putExtra(FutureSelfCallActivity.EXTRA_DURATION_MINUTES, 90)
                                putExtra(FutureSelfCallActivity.EXTRA_XP_REWARD, 150)
                                putExtra(FutureSelfCallActivity.EXTRA_STUDENT_NAME, "Rudra")
                                putExtra(FutureSelfCallActivity.EXTRA_CUSTOM_QUOTE, customQuote)
                                putExtra(FutureSelfCallActivity.EXTRA_DELAY_MINUTES, 0)
                                putExtra(FutureSelfCallActivity.EXTRA_IS_MAX_DELAY, false)
                            }
                            context.startActivity(testIntent)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF0284C7)
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Campaign,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "TEST STANDARD INCOMING CALL",
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            fontSize = 12.sp,
                            letterSpacing = 1.sp
                        )
                    }

                    // Test Max Delay Trigger Call
                    Button(
                        onClick = {
                            val testIntent = Intent(context, FutureSelfCallActivity::class.java).apply {
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                                putExtra(FutureSelfCallActivity.EXTRA_TASK_ID, 9999L)
                                putExtra(FutureSelfCallActivity.EXTRA_TASK_TITLE, "Physics Electrostatics")
                                putExtra(FutureSelfCallActivity.EXTRA_TASK_SUBJECT, "Physics")
                                putExtra(FutureSelfCallActivity.EXTRA_START_TIME, "05:00 AM")
                                putExtra(FutureSelfCallActivity.EXTRA_END_TIME, "06:30 AM")
                                putExtra(FutureSelfCallActivity.EXTRA_DURATION_MINUTES, 90)
                                putExtra(FutureSelfCallActivity.EXTRA_XP_REWARD, 150)
                                putExtra(FutureSelfCallActivity.EXTRA_STUDENT_NAME, "Rudra")
                                putExtra(FutureSelfCallActivity.EXTRA_CUSTOM_QUOTE, customQuote)
                                putExtra(FutureSelfCallActivity.EXTRA_DELAY_MINUTES, settings.maxAllowedDelayMinutes)
                                putExtra(FutureSelfCallActivity.EXTRA_IS_MAX_DELAY, true)
                            }
                            context.startActivity(testIntent)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFDC2626)
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Bolt,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "TEST MAX DELAY AUTO-PLAY CALL",
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            fontSize = 12.sp,
                            letterSpacing = 1.sp
                        )
                    }
                }
                Spacer(modifier = Modifier.height(30.dp))
            }
        }
    }
}

@Composable
fun SettingsSectionContainer(
    title: String,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
        border = BorderStroke(1.dp, Color(0xFF1E293B))
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(
                text = title,
                color = Color(0xFF38BDF8),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.5.sp
            )
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}
