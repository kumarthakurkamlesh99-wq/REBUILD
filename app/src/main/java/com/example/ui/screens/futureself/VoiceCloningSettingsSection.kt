package com.example.ui.screens.futureself

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AudioFile
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.data.model.futureself.FutureSelfVoiceSettings
import com.example.data.model.futureself.VoiceCloneProvider
import com.example.speech.AudioRecorderHelper
import com.example.speech.FutureSelfSpeechManager
import com.example.speech.VoiceCloningService
import com.example.util.RingtoneStorageManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File

@Composable
fun VoiceCloningSettingsSection(
    settings: FutureSelfVoiceSettings,
    onSaveSettings: (FutureSelfVoiceSettings) -> Unit,
    speechManager: FutureSelfSpeechManager
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var isVoiceCloningEnabled by remember(settings) { mutableStateOf(settings.isVoiceCloningEnabled) }
    var selectedProvider by remember(settings) { mutableStateOf(settings.voiceCloneProvider) }
    var omniVoiceEndpoint by remember(settings) { mutableStateOf(settings.omniVoiceEndpointUrl) }
    var voiceCloneId by remember(settings) { mutableStateOf(settings.voiceCloneId) }
    var elevenLabsApiKey by remember(settings) { mutableStateOf(settings.elevenLabsApiKey) }
    var stability by remember(settings) { mutableFloatStateOf(settings.voiceCloneStability) }
    var similarity by remember(settings) { mutableFloatStateOf(settings.voiceCloneSimilarity) }

    var showRecordDialog by remember { mutableStateOf(false) }
    var isCloningInProgress by remember { mutableStateOf(false) }
    var cloningStatusMessage by remember { mutableStateOf<String?>(null) }
    var isTestingVoice by remember { mutableStateOf(false) }
    var testVoiceStatusMessage by remember { mutableStateOf<String?>(null) }
    var isApiKeyVisible by remember { mutableStateOf(false) }
    var isPlayingSamplePreview by remember { mutableStateOf(false) }

    val voiceCloneSamplePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            val result = RingtoneStorageManager.saveCustomRingtone(context, uri)
            onSaveSettings(
                settings.copy(
                    voiceCloneSampleUri = result.storedUriOrPath,
                    voiceCloneSampleName = result.displayName
                )
            )
        }
    }

    val recordAudioPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            showRecordDialog = true
        }
    }

    // Voice Sample In-App Recording Dialog
    if (showRecordDialog) {
        VoiceSampleRecordingDialog(
            context = context,
            speechManager = speechManager,
            volume = settings.volume,
            onDismiss = { showRecordDialog = false },
            onSampleSaved = { path, name ->
                onSaveSettings(
                    settings.copy(
                        voiceCloneSampleUri = path,
                        voiceCloneSampleName = name
                    )
                )
                showRecordDialog = false
            }
        )
    }

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        // Toggle Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
            border = BorderStroke(
                1.dp,
                if (isVoiceCloningEnabled) Color(0xFFA855F7).copy(alpha = 0.6f) else Color(0xFF334155)
            )
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
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isVoiceCloningEnabled) Color(0xFFA855F7).copy(alpha = 0.2f) else Color(0xFF334155),
                        modifier = Modifier.size(42.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.RecordVoiceOver,
                                contentDescription = null,
                                tint = if (isVoiceCloningEnabled) Color(0xFFA855F7) else Color(0xFF94A3B8),
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Real Voice Cloning",
                                color = Color.White,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                color = Color(0xFFA855F7).copy(alpha = 0.2f),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = "AI NEURAL",
                                    color = Color(0xFFD8B4FE),
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = if (isVoiceCloningEnabled) "Future Self speaks in YOUR cloned voice" else "Using standard synthetic voice",
                            color = if (isVoiceCloningEnabled) Color(0xFFC084FC) else Color(0xFF94A3B8),
                            fontSize = 12.sp
                        )
                    }
                }

                Switch(
                    checked = isVoiceCloningEnabled,
                    onCheckedChange = { checked ->
                        isVoiceCloningEnabled = checked
                        onSaveSettings(settings.copy(isVoiceCloningEnabled = checked))
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color(0xFFA855F7),
                        checkedTrackColor = Color(0xFF9333EA).copy(alpha = 0.5f)
                    )
                )
            }
        }

        if (isVoiceCloningEnabled) {
            // Explanation Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                border = BorderStroke(1.dp, Color(0xFF6B21A8).copy(alpha = 0.4f))
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = Color(0xFFA855F7),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Upload or record your 20-45s voice sample. The neural AI engine builds a digital clone of your vocal chords, allowing Future Self to speak any new task call dynamically in your voice!",
                        color = Color(0xFFCBD5E1),
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )
                }
            }

            // 1. Voice Sample Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                border = BorderStroke(1.dp, Color(0xFF334155))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "STEP 1: YOUR VOICE SAMPLE",
                        color = Color(0xFF38BDF8),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    if (settings.voiceCloneSampleUri.isNotBlank()) {
                        Surface(
                            color = Color(0xFF0F172A),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AudioFile,
                                        contentDescription = null,
                                        tint = Color(0xFF10B981),
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = settings.voiceCloneSampleName.ifBlank { "Voice Sample" },
                                        color = Color.White,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium,
                                        maxLines = 1
                                    )
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(
                                        onClick = {
                                            if (isPlayingSamplePreview) {
                                                speechManager.stopRingtoneAndVibrate()
                                                isPlayingSamplePreview = false
                                            } else {
                                                isPlayingSamplePreview = true
                                                speechManager.playCustomAudioOrSpeech(
                                                    customAudioPath = settings.voiceCloneSampleUri,
                                                    fallbackText = "Sample playback",
                                                    volume = settings.volume,
                                                    onDone = { isPlayingSamplePreview = false }
                                                )
                                            }
                                        },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (isPlayingSamplePreview) Icons.Default.Stop else Icons.Default.PlayArrow,
                                            contentDescription = "Listen",
                                            tint = Color(0xFF38BDF8)
                                        )
                                    }
                                    IconButton(
                                        onClick = {
                                            speechManager.stopRingtoneAndVibrate()
                                            isPlayingSamplePreview = false
                                            onSaveSettings(
                                                settings.copy(
                                                    voiceCloneSampleUri = "",
                                                    voiceCloneSampleName = ""
                                                )
                                            )
                                        },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Remove",
                                            tint = Color(0xFFEF4444)
                                        )
                                    }
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                val hasPermission = ContextCompat.checkSelfPermission(
                                    context,
                                    Manifest.permission.RECORD_AUDIO
                                ) == PackageManager.PERMISSION_GRANTED
                                if (hasPermission) {
                                    showRecordDialog = true
                                } else {
                                    recordAudioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                }
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Mic, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "Record Mic", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = {
                                voiceCloneSamplePickerLauncher.launch("audio/*")
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, Color(0xFF475569))
                        ) {
                            Icon(imageVector = Icons.Default.CloudUpload, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "Upload File", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // 2. AI Engine & Provider Choice Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                border = BorderStroke(1.dp, Color(0xFF334155))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "STEP 2: AI VOICE CLONE ENGINE",
                        color = Color(0xFFA855F7),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    // Provider Tabs
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        VoiceCloneProvider.values().forEach { provider ->
                            val isSelected = selectedProvider == provider
                            Button(
                                onClick = {
                                    selectedProvider = provider
                                    onSaveSettings(settings.copy(voiceCloneProvider = provider))
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isSelected) Color(0xFFA855F7) else Color(0xFF0F172A)
                                ),
                                border = BorderStroke(1.dp, if (isSelected) Color(0xFFC084FC) else Color(0xFF334155))
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = if (provider == VoiceCloneProvider.OMNI_VOICE) "OmniVoice (k2-fsa)" else "ElevenLabs AI",
                                        color = if (isSelected) Color.White else Color(0xFF94A3B8),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = provider.badge,
                                        color = if (isSelected) Color(0xFFFDE047) else Color(0xFF64748B),
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(14.dp))

                    if (selectedProvider == VoiceCloneProvider.OMNI_VOICE) {
                        // OmniVoice (100% Free Open-Source Zero-Shot)
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.4f))
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        color = Color(0xFF10B981).copy(alpha = 0.2f),
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            text = "100% FREE • OPEN SOURCE",
                                            color = Color(0xFF34D399),
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "k2-fsa Zero-Shot Cloning",
                                        color = Color.White,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "OmniVoice clones your voice directly from your Step 1 audio sample in real-time across 600+ languages without needing any API key or subscription!",
                                    color = Color(0xFFCBD5E1),
                                    fontSize = 11.sp,
                                    lineHeight = 15.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = omniVoiceEndpoint,
                            onValueChange = {
                                omniVoiceEndpoint = it
                                onSaveSettings(settings.copy(omniVoiceEndpointUrl = it))
                            },
                            label = { Text("OmniVoice Server Endpoint", color = Color(0xFF94A3B8)) },
                            placeholder = { Text("https://k2-fsa-omnivoice.hf.space", color = Color(0xFF64748B)) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF10B981),
                                unfocusedBorderColor = Color(0xFF475569),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            shape = RoundedCornerShape(10.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Free public HuggingFace Space (k2-fsa/OmniVoice) or custom server",
                            color = Color(0xFF94A3B8),
                            fontSize = 11.sp
                        )
                    } else {
                        // ElevenLabs AI Option
                        OutlinedTextField(
                            value = elevenLabsApiKey,
                            onValueChange = {
                                elevenLabsApiKey = it
                                onSaveSettings(settings.copy(elevenLabsApiKey = it))
                            },
                            label = { Text("ElevenLabs API Key", color = Color(0xFF94A3B8)) },
                            placeholder = { Text("Enter your API key", color = Color(0xFF64748B)) },
                            singleLine = true,
                            visualTransformation = if (isApiKeyVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            trailingIcon = {
                                IconButton(onClick = { isApiKeyVisible = !isApiKeyVisible }) {
                                    Icon(
                                        imageVector = if (isApiKeyVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = null,
                                        tint = Color(0xFF94A3B8)
                                    )
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFFA855F7),
                                unfocusedBorderColor = Color(0xFF475569),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            shape = RoundedCornerShape(10.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Free API key from elevenlabs.io (or leave empty if configured in .env)",
                            color = Color(0xFF94A3B8),
                            fontSize = 11.sp
                        )
                        Spacer(modifier = Modifier.height(14.dp))

                        Button(
                            onClick = {
                                val samplePath = settings.voiceCloneSampleUri
                                if (samplePath.isBlank()) {
                                    cloningStatusMessage = "Please record or upload a voice sample first!"
                                    return@Button
                                }
                                val sampleFile = File(samplePath)
                                if (!sampleFile.exists()) {
                                    cloningStatusMessage = "Voice sample file not found on device!"
                                    return@Button
                                }

                                coroutineScope.launch {
                                    isCloningInProgress = true
                                    cloningStatusMessage = "Cloning your voice with ElevenLabs AI neural engine..."
                                    val result = VoiceCloningService.getInstance(context).cloneVoiceFromAudioFile(
                                        audioFile = sampleFile,
                                        voiceName = "Future Self Clone",
                                        apiKey = elevenLabsApiKey
                                    )
                                    isCloningInProgress = false
                                    if (result.isSuccess) {
                                        val cloneResult = result.getOrNull()!!
                                        voiceCloneId = cloneResult.voiceId
                                        onSaveSettings(
                                            settings.copy(
                                                voiceCloneId = cloneResult.voiceId,
                                                voiceCloneName = cloneResult.voiceName,
                                                isVoiceCloningEnabled = true
                                            )
                                        )
                                        cloningStatusMessage = "Success! Voice Cloned: ${cloneResult.voiceId}"
                                    } else {
                                        cloningStatusMessage = "Cloning failed: ${result.exceptionOrNull()?.message}"
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !isCloningInProgress && settings.voiceCloneSampleUri.isNotBlank(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF9333EA),
                                disabledContainerColor = Color(0xFF475569)
                            ),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            if (isCloningInProgress) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = "Cloning Voice...", color = Color.White, fontWeight = FontWeight.Bold)
                            } else {
                                Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = "Clone My Voice With AI", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }

                        if (cloningStatusMessage != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = cloningStatusMessage!!,
                                color = if (cloningStatusMessage!!.startsWith("Success")) Color(0xFF10B981) else Color(0xFFF87171),
                                fontSize = 12.sp,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            }

            // 3. Active Voice ID & Presets Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                border = BorderStroke(1.dp, Color(0xFF334155))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "ACTIVE VOICE ID & PRESETS",
                        color = Color(0xFF38BDF8),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = voiceCloneId,
                        onValueChange = {
                            voiceCloneId = it
                            onSaveSettings(settings.copy(voiceCloneId = it))
                        },
                        label = { Text("Active Voice ID", color = Color(0xFF94A3B8)) },
                        placeholder = { Text("e.g. onwK4e9ZLuTAKqWW03F9", color = Color(0xFF64748B)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF38BDF8),
                            unfocusedBorderColor = Color(0xFF475569),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Or choose a preset AI voice profile:",
                        color = Color(0xFF94A3B8),
                        fontSize = 11.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        VoiceCloningService.PRESET_VOICES.forEach { preset ->
                            val isSelected = voiceCloneId == preset.id
                            Surface(
                                color = if (isSelected) Color(0xFF0284C7).copy(alpha = 0.25f) else Color(0xFF0F172A),
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, if (isSelected) Color(0xFF38BDF8) else Color(0xFF334155)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        voiceCloneId = preset.id
                                        onSaveSettings(
                                            settings.copy(
                                                voiceCloneId = preset.id,
                                                voiceCloneName = preset.name
                                            )
                                        )
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(text = preset.name, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        Text(text = preset.description, color = Color(0xFF94A3B8), fontSize = 11.sp)
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

            // 4. Fine-Tuning Sliders
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                border = BorderStroke(1.dp, Color(0xFF334155))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "NEURAL VOICE TUNING",
                        color = Color(0xFF38BDF8),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Voice Stability", color = Color.White, fontSize = 13.sp)
                        Text(text = "${(stability * 100).toInt()}%", color = Color(0xFF38BDF8), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                    Slider(
                        value = stability,
                        onValueChange = {
                            stability = it
                            onSaveSettings(settings.copy(voiceCloneStability = it))
                        },
                        valueRange = 0.1f..1.0f,
                        colors = SliderDefaults.colors(
                            thumbColor = Color(0xFF38BDF8),
                            activeTrackColor = Color(0xFF0284C7)
                        )
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Clarity & Similarity Boost", color = Color.White, fontSize = 13.sp)
                        Text(text = "${(similarity * 100).toInt()}%", color = Color(0xFFA855F7), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                    Slider(
                        value = similarity,
                        onValueChange = {
                            similarity = it
                            onSaveSettings(settings.copy(voiceCloneSimilarity = it))
                        },
                        valueRange = 0.1f..1.0f,
                        colors = SliderDefaults.colors(
                            thumbColor = Color(0xFFA855F7),
                            activeTrackColor = Color(0xFF9333EA)
                        )
                    )
                }
            }

            // 5. Test Cloned Voice Preview
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                border = BorderStroke(1.dp, Color(0xFF00F0FF).copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.GraphicEq, contentDescription = null, tint = Color(0xFF00F0FF), modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "TEST FUTURE SELF CALL VOICE", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Hear how your Future Self will sound when it rings and delivers your ignition directive.",
                        color = Color(0xFF94A3B8),
                        fontSize = 11.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                val sampleSentence = "Kamlesh, this is your future self. We don't negotiate with resistance. Let's get to work now."

                                coroutineScope.launch {
                                    isTestingVoice = true
                                    testVoiceStatusMessage = "Synthesizing dynamic call speech..."

                                    val res = if (selectedProvider == VoiceCloneProvider.OMNI_VOICE) {
                                        val samplePath = settings.voiceCloneSampleUri
                                        val sampleFile = File(samplePath)
                                        if (samplePath.isBlank() || !sampleFile.exists()) {
                                            isTestingVoice = false
                                            testVoiceStatusMessage = "Please record or upload a voice sample in Step 1 first!"
                                            return@launch
                                        }
                                        VoiceCloningService.getInstance(context).synthesizeWithOmniVoice(
                                            text = sampleSentence,
                                            sampleAudioFile = sampleFile,
                                            endpointUrl = omniVoiceEndpoint,
                                            languageCode = settings.language.localeCode
                                        )
                                    } else {
                                        val resolvedTargetVoiceId = voiceCloneId.ifBlank { "onwK4e9ZLuTAKqWW03F9" }
                                        VoiceCloningService.getInstance(context).synthesizeSpeech(
                                            text = sampleSentence,
                                            voiceId = resolvedTargetVoiceId,
                                            apiKey = elevenLabsApiKey,
                                            stability = stability,
                                            similarity = similarity
                                        )
                                    }

                                    isTestingVoice = false
                                    if (res.isSuccess) {
                                        val audioFile = res.getOrNull()!!
                                        testVoiceStatusMessage = "Playing cloned voice preview..."
                                        speechManager.playCustomAudioOrSpeech(
                                            customAudioPath = audioFile.absolutePath,
                                            fallbackText = sampleSentence,
                                            volume = settings.volume,
                                            onDone = {
                                                testVoiceStatusMessage = null
                                            }
                                        )
                                    } else {
                                        testVoiceStatusMessage = "Preview note: ${res.exceptionOrNull()?.message}"
                                    }
                                }
                            },
                            modifier = Modifier.weight(1f),
                            enabled = !isTestingVoice,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00F0FF)),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            if (isTestingVoice) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.Black, strokeWidth = 2.dp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(text = "Generating...", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            } else {
                                Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, tint = Color.Black, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(text = "Test Cloned Voice", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }

                        if (testVoiceStatusMessage != null) {
                            OutlinedButton(
                                onClick = {
                                    speechManager.stopSpeaking()
                                    speechManager.stopRingtoneAndVibrate()
                                    testVoiceStatusMessage = null
                                },
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, Color(0xFFEF4444))
                            ) {
                                Icon(imageVector = Icons.Default.Stop, contentDescription = null, tint = Color(0xFFEF4444), modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(text = "Stop", color = Color(0xFFEF4444), fontSize = 12.sp)
                            }
                        }
                    }

                    if (testVoiceStatusMessage != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = testVoiceStatusMessage!!,
                            color = if (testVoiceStatusMessage!!.contains("failed")) Color(0xFFF87171) else Color(0xFF00F0FF),
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun VoiceSampleRecordingDialog(
    context: Context,
    speechManager: FutureSelfSpeechManager,
    volume: Float,
    onDismiss: () -> Unit,
    onSampleSaved: (path: String, name: String) -> Unit
) {
    val audioRecorder = remember { AudioRecorderHelper(context) }
    var isRecording by remember { mutableStateOf(false) }
    var recordingDurationSec by remember { mutableIntStateOf(0) }
    var isPlayingPreview by remember { mutableStateOf(false) }

    val sampleFile = remember {
        File(context.filesDir, "voice_samples/my_future_self_sample.m4a")
    }

    LaunchedEffect(isRecording) {
        if (isRecording) {
            recordingDurationSec = 0
            while (isRecording) {
                delay(1000)
                recordingDurationSec++
            }
        }
    }

    AlertDialog(
        onDismissRequest = {
            if (isRecording) {
                audioRecorder.stopRecording()
            }
            speechManager.stopRingtoneAndVibrate()
            onDismiss()
        },
        containerColor = Color(0xFF0F172A),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Mic,
                    contentDescription = null,
                    tint = Color(0xFF00F0FF),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Record Voice Sample",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text(
                    text = "For high-fidelity AI voice cloning, speak naturally for 20-45 seconds in a quiet room.",
                    color = Color(0xFF94A3B8),
                    fontSize = 12.sp
                )

                // Suggested Script Prompt Card
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Color(0xFF334155))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "SUGGESTED SCRIPT TO READ ALOUD:",
                            color = Color(0xFF38BDF8),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "\"I am taking full control of my time, my discipline, and my future. When my future self calls, I will not make excuses. I will rise to the challenge and execute with relentless focus.\"",
                            color = Color.White,
                            fontSize = 13.sp,
                            lineHeight = 18.sp
                        )
                    }
                }

                // Recording Status / Timer
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = if (isRecording) Color(0xFFEF4444).copy(alpha = 0.15f) else Color(0xFF1E293B),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .padding(14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (isRecording) {
                            Surface(
                                shape = CircleShape,
                                color = Color(0xFFEF4444),
                                modifier = Modifier.size(12.dp)
                            ) {}
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "RECORDING: ${String.format("%02d:%02d", recordingDurationSec / 60, recordingDurationSec % 60)}",
                                color = Color(0xFFEF4444),
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        } else if (sampleFile.exists() && sampleFile.length() > 500) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = Color(0xFF10B981),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Sample Ready (${sampleFile.length() / 1024} KB)",
                                color = Color(0xFF10B981),
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        } else {
                            Text(
                                text = "Ready to record (Press Start below)",
                                color = Color(0xFF94A3B8),
                                fontSize = 13.sp
                            )
                        }
                    }
                }

                // Record / Stop Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (!isRecording) {
                        Button(
                            onClick = {
                                audioRecorder.startRecording(sampleFile)
                                isRecording = true
                                isPlayingPreview = false
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Mic, contentDescription = null, tint = Color.White)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "Start", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Button(
                            onClick = {
                                audioRecorder.stopRecording()
                                isRecording = false
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Stop, contentDescription = null, tint = Color.White)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "Stop", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }

                    if (!isRecording && sampleFile.exists() && sampleFile.length() > 500) {
                        OutlinedButton(
                            onClick = {
                                if (isPlayingPreview) {
                                    speechManager.stopRingtoneAndVibrate()
                                    isPlayingPreview = false
                                } else {
                                    isPlayingPreview = true
                                    speechManager.playCustomAudioOrSpeech(
                                        customAudioPath = sampleFile.absolutePath,
                                        fallbackText = "Test sample",
                                        volume = volume,
                                        onDone = { isPlayingPreview = false }
                                    )
                                }
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, Color(0xFF38BDF8))
                        ) {
                            Icon(
                                imageVector = if (isPlayingPreview) Icons.Default.Stop else Icons.Default.PlayArrow,
                                contentDescription = null,
                                tint = Color(0xFF38BDF8)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isPlayingPreview) "Stop" else "Listen",
                                color = Color(0xFF38BDF8),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (isRecording) {
                        audioRecorder.stopRecording()
                        isRecording = false
                    }
                    speechManager.stopRingtoneAndVibrate()
                    isPlayingPreview = false
                    if (sampleFile.exists() && sampleFile.length() > 500) {
                        onSampleSaved(
                            sampleFile.absolutePath,
                            "Recorded Voice Sample (${recordingDurationSec}s)"
                        )
                    } else {
                        onDismiss()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00F0FF)),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(text = "Use This Recording", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    if (isRecording) {
                        audioRecorder.stopRecording()
                        isRecording = false
                    }
                    speechManager.stopRingtoneAndVibrate()
                    isPlayingPreview = false
                    onDismiss()
                }
            ) {
                Text(text = "Cancel", color = Color(0xFF94A3B8))
            }
        }
    )
}
