package com.example.ui.screens.futureself

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AudioFile
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.data.model.futureself.FutureSelfVoiceSettings
import com.example.speech.AudioRecorderHelper
import com.example.speech.FutureSelfSpeechManager
import com.example.util.RingtoneStorageManager
import kotlinx.coroutines.delay
import java.io.File

private val DarkCardBg = Color(0xFF0E1629)
private val DarkCardBorder = Color(0x337C8CFF)
private val AccentPrimary = Color(0xFF7C8CFF)
private val AccentGlow = Color(0xFF60A5FA)
private val GreenActive = Color(0xFF4ADE80)
private val RedDanger = Color(0xFFF87171)
private val MutedText = Color(0xFF94A3B8)

@Composable
fun CustomCallAudioSection(
    settings: FutureSelfVoiceSettings,
    onSaveSettings: (FutureSelfVoiceSettings) -> Unit,
    speechManager: FutureSelfSpeechManager
) {
    val context = LocalContext.current
    var isPlayingPreview by remember { mutableStateOf(false) }
    var showRecordDialog by remember { mutableStateOf(false) }

    val audioFilePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            val result = RingtoneStorageManager.saveCustomRingtone(context, uri)
            onSaveSettings(
                settings.copy(
                    customCallAudioUri = result.storedUriOrPath,
                    customCallAudioName = result.displayName,
                    useCustomCallAudio = true
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

    if (showRecordDialog) {
        CleanAudioRecordingDialog(
            context = context,
            speechManager = speechManager,
            volume = settings.volume,
            onDismiss = { showRecordDialog = false },
            onAudioSaved = { path, name ->
                onSaveSettings(
                    settings.copy(
                        customCallAudioUri = path,
                        customCallAudioName = name,
                        useCustomCallAudio = true
                    )
                )
                showRecordDialog = false
            }
        )
    }

    val hasActiveCustomAudio = settings.customCallAudioUri.isNotBlank() &&
            File(settings.customCallAudioUri).let { it.exists() && it.length() > 200 }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // Toggle Switch Card
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = DarkCardBg,
            border = BorderStroke(
                1.dp,
                if (settings.useCustomCallAudio && hasActiveCustomAudio) GreenActive.copy(alpha = 0.5f) else DarkCardBorder
            ),
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
                                if (settings.useCustomCallAudio && hasActiveCustomAudio)
                                    GreenActive.copy(alpha = 0.15f)
                                else AccentPrimary.copy(alpha = 0.12f)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.GraphicEq,
                            contentDescription = null,
                            tint = if (settings.useCustomCallAudio && hasActiveCustomAudio) GreenActive else AccentGlow,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Play Custom Audio on Answer",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = if (hasActiveCustomAudio && settings.useCustomCallAudio)
                                "Audio will play as soon as call is answered"
                            else if (hasActiveCustomAudio)
                                "Audio uploaded but disabled (TTS will speak)"
                            else
                                "Upload or record audio to play on call answer",
                            color = if (hasActiveCustomAudio && settings.useCustomCallAudio) GreenActive else MutedText,
                            fontSize = 11.sp
                        )
                    }
                }

                Switch(
                    checked = settings.useCustomCallAudio,
                    onCheckedChange = { checked ->
                        onSaveSettings(settings.copy(useCustomCallAudio = checked))
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = GreenActive,
                        uncheckedThumbColor = MutedText,
                        uncheckedTrackColor = Color(0xFF1E293B)
                    )
                )
            }
        }

        // Active Audio Card OR Empty State Card
        if (hasActiveCustomAudio) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFF091122),
                border = BorderStroke(1.dp, GreenActive.copy(alpha = 0.35f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
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
                                tint = GreenActive,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = settings.customCallAudioName.ifBlank { "Custom Audio File" },
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "Ready • Plays through speaker upon answering",
                                    color = GreenActive,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = GreenActive.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "ACTIVE",
                                color = GreenActive,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Action buttons: Preview / Change / Delete
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = {
                                if (isPlayingPreview) {
                                    speechManager.stopRingtoneAndVibrate()
                                    isPlayingPreview = false
                                } else {
                                    isPlayingPreview = true
                                    speechManager.playCustomAudioOrSpeech(
                                        customAudioPath = settings.customCallAudioUri,
                                        fallbackText = "Testing custom call audio",
                                        volume = settings.volume,
                                        onDone = { isPlayingPreview = false }
                                    )
                                }
                            },
                            modifier = Modifier.weight(1.3f),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isPlayingPreview) RedDanger else AccentPrimary
                            )
                        ) {
                            Icon(
                                imageVector = if (isPlayingPreview) Icons.Default.Stop else Icons.Default.PlayArrow,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isPlayingPreview) "Stop Preview" else "Listen Audio",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        OutlinedButton(
                            onClick = { audioFilePickerLauncher.launch("audio/*") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, DarkCardBorder)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CloudUpload,
                                contentDescription = null,
                                tint = AccentGlow,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Change", color = AccentGlow, fontSize = 12.sp)
                        }

                        IconButton(
                            onClick = {
                                speechManager.stopRingtoneAndVibrate()
                                isPlayingPreview = false
                                onSaveSettings(
                                    settings.copy(
                                        customCallAudioUri = "",
                                        customCallAudioName = ""
                                    )
                                )
                            },
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(RedDanger.copy(alpha = 0.12f))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete audio",
                                tint = RedDanger,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        } else {
            // Clean empty state card
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = DarkCardBg,
                border = BorderStroke(1.dp, DarkCardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "No custom call audio selected. Upload an audio file or record your voice message to play when you answer Future Self calls.",
                        color = MutedText,
                        fontSize = 12.sp,
                        lineHeight = 17.sp,
                        modifier = Modifier.padding(bottom = 14.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = { audioFilePickerLauncher.launch("audio/*") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = AccentPrimary)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CloudUpload,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Upload Audio",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        OutlinedButton(
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
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, GreenActive.copy(alpha = 0.8f))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Mic,
                                contentDescription = null,
                                tint = GreenActive,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Record Voice",
                                color = GreenActive,
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

@Composable
fun CleanAudioRecordingDialog(
    context: Context,
    speechManager: FutureSelfSpeechManager,
    volume: Float,
    onDismiss: () -> Unit,
    onAudioSaved: (filePath: String, displayName: String) -> Unit
) {
    val recorder = remember { AudioRecorderHelper(context) }
    var isRecording by remember { mutableStateOf(false) }
    var elapsedSeconds by remember { mutableIntStateOf(0) }
    var recordedFile by remember { mutableStateOf<File?>(null) }
    var isPreviewing by remember { mutableStateOf(false) }

    LaunchedEffect(isRecording) {
        if (isRecording) {
            elapsedSeconds = 0
            while (isRecording) {
                delay(1000L)
                elapsedSeconds++
            }
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    AlertDialog(
        onDismissRequest = {
            if (isRecording) recorder.stopRecording()
            speechManager.stopRingtoneAndVibrate()
            onDismiss()
        },
        containerColor = Color(0xFF0A0F1D),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(GreenActive.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = null,
                        tint = GreenActive,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Record Call Audio",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "Record a short motivating voice message (e.g. 'Start studying now, no more excuses!'). When you answer the call, this audio will play immediately.",
                    color = MutedText,
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )

                // Timer Display Box
                Surface(
                    color = DarkCardBg,
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(
                        1.dp,
                        if (isRecording) RedDanger.copy(alpha = pulseAlpha) else DarkCardBorder
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = String.format(java.util.Locale.US, "%02d:%02d", elapsedSeconds / 60, elapsedSeconds % 60),
                            color = if (isRecording) RedDanger else if (recordedFile != null) GreenActive else Color.White,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = when {
                                isRecording -> "Recording in progress..."
                                recordedFile != null -> "Recording ready • Listen or Save"
                                else -> "Tap 'Start Recording' when ready"
                            },
                            color = MutedText,
                            fontSize = 11.sp
                        )
                    }
                }

                // Recording Action Buttons
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (!isRecording && recordedFile == null) {
                        Button(
                            onClick = {
                                val outFile = File(context.filesDir, "custom_call_record_${System.currentTimeMillis()}.m4a")
                                val success = recorder.startRecording(outFile)
                                if (success) {
                                    recordedFile = outFile
                                    isRecording = true
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = RedDanger),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Mic, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Start Recording", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    } else if (isRecording) {
                        Button(
                            onClick = {
                                recorder.stopRecording()
                                isRecording = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = GreenActive),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Stop, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Stop", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    } else {
                        OutlinedButton(
                            onClick = {
                                if (isPreviewing) {
                                    speechManager.stopRingtoneAndVibrate()
                                    isPreviewing = false
                                } else {
                                    val file = recordedFile
                                    if (file != null && file.exists()) {
                                        isPreviewing = true
                                        speechManager.playCustomAudioOrSpeech(
                                            customAudioPath = file.absolutePath,
                                            fallbackText = "Preview",
                                            volume = volume,
                                            onDone = { isPreviewing = false }
                                        )
                                    }
                                }
                            },
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, AccentGlow)
                        ) {
                            Icon(
                                imageVector = if (isPreviewing) Icons.Default.Stop else Icons.Default.PlayArrow,
                                contentDescription = null,
                                tint = AccentGlow,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (isPreviewing) "Stop" else "Listen", color = AccentGlow, fontSize = 12.sp)
                        }

                        TextButton(
                            onClick = {
                                recordedFile?.delete()
                                recordedFile = null
                                elapsedSeconds = 0
                            }
                        ) {
                            Text("Re-record", color = MutedText, fontSize = 12.sp)
                        }
                    }
                }
            }
        },
        confirmButton = {
            if (recordedFile != null && !isRecording) {
                Button(
                    onClick = {
                        val file = recordedFile
                        if (file != null && file.exists()) {
                            onAudioSaved(file.absolutePath, "Voice Message (${elapsedSeconds}s)")
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GreenActive),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Save Audio", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    if (isRecording) recorder.stopRecording()
                    speechManager.stopRingtoneAndVibrate()
                    onDismiss()
                }
            ) {
                Text("Cancel", color = MutedText, fontSize = 12.sp)
            }
        }
    )
}
