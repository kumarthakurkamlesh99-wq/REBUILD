package com.example.ui.screens

import android.content.Context
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.AudioFile
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PhoneInTalk
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import com.example.ui.screens.futureself.CustomCallAudioSection
import com.example.util.RingtoneStorageManager

private val ScreenBg = Color(0xFF050816)
private val CardBg = Color(0xFF0E1629)
private val CardBorder = Color(0x337C8CFF)
private val AccentColor = Color(0xFF7C8CFF)
private val AccentLight = Color(0xFF60A5FA)
private val EmeraldSuccess = Color(0xFF4ADE80)
private val AmberWarning = Color(0xFFFACC15)
private val TextMuted = Color(0xFF94A3B8)

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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ScreenBg)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        // Clean Top App Bar
        Surface(
            color = ScreenBg,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(CardBg)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Future Self Calls",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Call audio, alerts & discipline triggers",
                            color = TextMuted,
                            fontSize = 11.sp
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = if (isEnabled) EmeraldSuccess.copy(alpha = 0.15f) else Color(0xFF1E293B),
                    border = BorderStroke(
                        1.dp,
                        if (isEnabled) EmeraldSuccess.copy(alpha = 0.4f) else Color(0xFF334155)
                    )
                ) {
                    Text(
                        text = if (isEnabled) "ACTIVE" else "OFF",
                        color = if (isEnabled) EmeraldSuccess else TextMuted,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Master Toggle Card
            item {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = CardBg,
                    border = BorderStroke(1.dp, if (isEnabled) EmeraldSuccess.copy(alpha = 0.4f) else CardBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
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
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        if (isEnabled) EmeraldSuccess.copy(alpha = 0.15f)
                                        else Color(0xFF1E293B)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PhoneInTalk,
                                    contentDescription = null,
                                    tint = if (isEnabled) EmeraldSuccess else TextMuted,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Enable Future Self Calls",
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = if (isEnabled) "Phone call triggers when scheduled task starts" else "Calls disabled (standard silent alerts)",
                                    color = if (isEnabled) EmeraldSuccess else TextMuted,
                                    fontSize = 11.sp
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
                                checkedThumbColor = Color.White,
                                checkedTrackColor = EmeraldSuccess,
                                uncheckedThumbColor = TextMuted,
                                uncheckedTrackColor = Color(0xFF1E293B)
                            )
                        )
                    }
                }
            }

            // Quick Test Button
            item {
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
                        .height(48.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AccentColor)
                ) {
                    Icon(
                        imageVector = Icons.Default.Campaign,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "TEST CALL (ANSWER TO HEAR AUDIO)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        letterSpacing = 0.5.sp
                    )
                }
            }

            // 1. Primary Feature: Custom Call Voice Audio Upload & Record
            item {
                CleanSettingsSection(
                    title = "CALL VOICE AUDIO",
                    subtitle = "Audio that plays immediately when you answer the call"
                ) {
                    CustomCallAudioSection(
                        settings = settings,
                        onSaveSettings = { updated -> save(updated) },
                        speechManager = speechManager
                    )
                }
            }

            // 2. Tactical Ringtone & Call Alerts
            item {
                CleanSettingsSection(
                    title = "RINGTONE & VIBRATION",
                    subtitle = "Sound pattern played while incoming call is ringing"
                ) {
                    val ringtones = listOf(
                        "CYBER_SIREN" to "Cyber Siren (Futuristic Pulse)",
                        "APEX_HORNS" to "Apex Horns (Cinematic War Horn)",
                        "ZEN_CHIME" to "Zen Chime (Deep Focus)",
                        "BELL" to "Classic Bell (Resonant)",
                        "MILITARY" to "Military Reveille (Urgent)",
                        "TICK_TOCK" to "Tick Tock (Time Running Out)"
                    )
                    val isCustomRingtone = selectedRingtone == "CUSTOM" || settings.ringtonePreset == "CUSTOM"

                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        // Custom Ringtone from Phone Storage
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isCustomRingtone) Color(0xFF131D38) else Color(0xFF091122),
                            border = BorderStroke(
                                1.dp,
                                if (isCustomRingtone) EmeraldSuccess else CardBorder
                            ),
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
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.MusicNote,
                                        contentDescription = null,
                                        tint = if (isCustomRingtone) EmeraldSuccess else TextMuted,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = "Custom Ringtone (From Phone)",
                                            color = Color.White,
                                            fontSize = 13.sp,
                                            fontWeight = if (isCustomRingtone) FontWeight.Bold else FontWeight.Medium
                                        )
                                        Text(
                                            text = if (settings.customRingtoneName.isNotBlank()) settings.customRingtoneName else "Tap to choose audio file from storage",
                                            color = if (isCustomRingtone) EmeraldSuccess else TextMuted,
                                            fontSize = 11.sp
                                        )
                                    }
                                }
                                if (isCustomRingtone) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = EmeraldSuccess,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }

                        // Preset Tactical Ringtones
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            ringtones.forEach { (key, label) ->
                                val isSelected = selectedRingtone == key && !isCustomRingtone
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (isSelected) Color(0xFF131D38) else Color(0xFF091122),
                                    border = BorderStroke(
                                        1.dp,
                                        if (isSelected) AccentLight else CardBorder.copy(alpha = 0.5f)
                                    ),
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
                                        Text(
                                            text = label,
                                            color = if (isSelected) Color.White else TextMuted,
                                            fontSize = 12.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                        )
                                        if (isSelected) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = null,
                                                tint = AccentLight,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        HorizontalDivider(color = CardBorder, thickness = 1.dp, modifier = Modifier.padding(vertical = 4.dp))

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
                                    tint = AmberWarning,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = "Haptic Vibration Pulse", color = Color.White, fontSize = 13.sp)
                            }
                            Switch(
                                checked = vibrationEnabled,
                                onCheckedChange = { checked ->
                                    vibrationEnabled = checked
                                    save(settings.copy(vibrationEnabled = checked))
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = AmberWarning,
                                    uncheckedThumbColor = TextMuted,
                                    uncheckedTrackColor = Color(0xFF1E293B)
                                )
                            )
                        }
                    }
                }
            }

            // 3. Fallback AI Speech & Directives (When no custom audio is active)
            item {
                CleanSettingsSection(
                    title = "FALLBACK SPEECH & TONE",
                    subtitle = "Used if custom audio is disabled or absent"
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        // Voice Gender
                        Column {
                            Text(text = "Voice Gender", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                VoiceGender.values().forEach { gender ->
                                    val isSelected = selectedGender == gender
                                    Surface(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clickable {
                                                selectedGender = gender
                                                save(settings.copy(voiceGender = gender))
                                            },
                                        shape = RoundedCornerShape(10.dp),
                                        color = if (isSelected) AccentColor else Color(0xFF091122),
                                        border = BorderStroke(1.dp, if (isSelected) AccentLight else CardBorder)
                                    ) {
                                        Box(
                                            modifier = Modifier.padding(vertical = 10.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = gender.displayName,
                                                color = if (isSelected) Color.White else TextMuted,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Voice Tone
                        Column {
                            Text(text = "Directive Tone", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            Spacer(modifier = Modifier.height(6.dp))
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                VoiceTone.values().forEach { tone ->
                                    val isSelected = selectedTone == tone
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = if (isSelected) Color(0xFF131D38) else Color(0xFF091122),
                                        border = BorderStroke(1.dp, if (isSelected) AccentLight else CardBorder),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                selectedTone = tone
                                                save(settings.copy(tone = tone))
                                            }
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Column {
                                                Text(
                                                    text = tone.displayName,
                                                    color = Color.White,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 13.sp
                                                )
                                                Text(
                                                    text = tone.description,
                                                    color = TextMuted,
                                                    fontSize = 11.sp
                                                )
                                            }
                                            if (isSelected) {
                                                Icon(
                                                    imageVector = Icons.Default.Check,
                                                    contentDescription = null,
                                                    tint = AccentLight,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Language Selection
                        Column {
                            Text(text = "Language", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                VoiceLanguage.values().forEach { lang ->
                                    val isSelected = selectedLanguage == lang
                                    Surface(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clickable {
                                                selectedLanguage = lang
                                                save(settings.copy(language = lang))
                                            },
                                        shape = RoundedCornerShape(10.dp),
                                        color = if (isSelected) AccentColor else Color(0xFF091122),
                                        border = BorderStroke(1.dp, if (isSelected) AccentLight else CardBorder)
                                    ) {
                                        Box(
                                            modifier = Modifier.padding(vertical = 10.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = lang.displayName,
                                                color = if (isSelected) Color.White else TextMuted,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Speed Slider
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = "Speech Speed", color = Color.White, fontSize = 12.sp)
                                Text(
                                    text = String.format(java.util.Locale.US, "%.1fx", speechSpeed),
                                    color = AccentLight,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
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
                                    thumbColor = AccentLight,
                                    activeTrackColor = AccentColor
                                )
                            )
                        }

                        // Volume Slider
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = "Call Volume", color = Color.White, fontSize = 12.sp)
                                Text(
                                    text = "${(volume * 100).toInt()}%",
                                    color = EmeraldSuccess,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
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
                                    thumbColor = EmeraldSuccess,
                                    activeTrackColor = EmeraldSuccess
                                )
                            )
                        }

                        // Custom Motivational Quote
                        Column {
                            Text(
                                text = "Custom Motivation Quote (Optional)",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            OutlinedTextField(
                                value = customQuote,
                                onValueChange = {
                                    customQuote = it
                                    save(settings.copy(customFutureSelfQuote = it))
                                },
                                modifier = Modifier.fillMaxWidth(),
                                placeholder = {
                                    Text("e.g. Top rank needs sacrifice today.", color = Color(0xFF475569), fontSize = 12.sp)
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = AccentLight,
                                    unfocusedBorderColor = CardBorder,
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                )
                            )
                        }
                    }
                }
            }

            // 4. Discipline Rules & Auto-Ignition
            item {
                CleanSettingsSection(
                    title = "DISCIPLINE RULES & TIMEOUT",
                    subtitle = "Automated triggers for procrastination delay"
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            text = "Max Allowed Delay Limit: ${settings.maxAllowedDelayMinutes} Minutes",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
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
                                    color = if (selected) AccentColor else Color(0xFF091122),
                                    shape = RoundedCornerShape(10.dp),
                                    border = BorderStroke(1.dp, if (selected) AccentLight else CardBorder)
                                ) {
                                    Box(
                                        modifier = Modifier.padding(vertical = 10.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "$limit m",
                                            color = if (selected) Color.White else TextMuted,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }

                        HorizontalDivider(color = CardBorder, thickness = 1.dp)

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
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "Immediately begins speaking directive when max delay hit",
                                    color = TextMuted,
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
                                    checkedTrackColor = EmeraldSuccess,
                                    uncheckedThumbColor = TextMuted,
                                    uncheckedTrackColor = Color(0xFF1E293B)
                                )
                            )
                        }

                        HorizontalDivider(color = CardBorder, thickness = 1.dp)

                        Text(
                            text = "Unanswered Call Ring Timeout: ${settings.callTimeoutSeconds}s",
                            color = Color.White,
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
                                    color = if (selected) AccentColor else Color(0xFF091122),
                                    shape = RoundedCornerShape(10.dp),
                                    border = BorderStroke(1.dp, if (selected) AccentLight else CardBorder)
                                ) {
                                    Box(
                                        modifier = Modifier.padding(vertical = 8.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "${sec}s",
                                            color = if (selected) Color.White else TextMuted,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Bottom Spacing
            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
fun CleanSettingsSection(
    title: String,
    subtitle: String? = null,
    content: @Composable () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = CardBg,
        border = BorderStroke(1.dp, CardBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                color = AccentLight,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.2.sp
            )
            if (!subtitle.isNullOrBlank()) {
                Text(
                    text = subtitle,
                    color = TextMuted,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
            Spacer(modifier = Modifier.height(14.dp))
            content()
        }
    }
}
