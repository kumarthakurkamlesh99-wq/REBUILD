package com.example.viewmodel

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.entity.AlarmChallengeType
import com.example.data.local.entity.AlarmDifficulty
import com.example.data.local.entity.AlarmEntity
import com.example.data.local.entity.AlarmLogEntity
import com.example.data.repository.RebuildRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AlarmsUiState(
    val alarms: List<AlarmEntity> = emptyList(),
    val alarmLogs: List<AlarmLogEntity> = emptyList(),
    val isCreatingOrEditing: Boolean = false,
    val editingAlarm: AlarmEntity? = null,
    val inputTitle: String = "Wake Up Protocol",
    val inputHour: Int = 6,
    val inputMinute: Int = 0,
    val inputChallengeType: AlarmChallengeType = AlarmChallengeType.MATH,
    val inputDifficulty: AlarmDifficulty = AlarmDifficulty.MEDIUM,
    val inputVolume: Int = 90,
    val inputVibrate: Boolean = true,
    val inputMaxSnoozes: Int = 3,
    val inputSnoozeDuration: Int = 5,
    val inputDaysOfWeek: String = "Mon,Tue,Wed,Thu,Fri,Sat,Sun",
    val inputRingtonePreset: String = "CYBER_SIREN",
    val inputSnoozeRingtonePreset: String = "TICK_TOCK",
    val isPreviewingSound: Boolean = false,
    val previewingToneName: String = ""
)

class AlarmsViewModel(
    private val rebuildRepository: RebuildRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AlarmsUiState())
    val uiState: StateFlow<AlarmsUiState> = _uiState.asStateFlow()

    private var previewMediaPlayer: MediaPlayer? = null

    init {
        viewModelScope.launch {
            rebuildRepository.getAllAlarms().collect { alarmsList ->
                if (alarmsList.isEmpty()) {
                    // Seed initial primary alarms
                    val defaultAlarms = listOf(
                        AlarmEntity(
                            title = "Apex Wake-Up Protocol",
                            hour = 6,
                            minute = 0,
                            isEnabled = true,
                            challengeType = AlarmChallengeType.MATH,
                            challengeDifficulty = AlarmDifficulty.MEDIUM,
                            volumePercent = 95,
                            isVibrationEnabled = true,
                            maxSnoozes = 3,
                            snoozeDurationMinutes = 5,
                            ringtonePreset = "CYBER_SIREN",
                            snoozeRingtonePreset = "TICK_TOCK"
                        ),
                        AlarmEntity(
                            title = "School Departure Call",
                            hour = 9,
                            minute = 15,
                            isEnabled = true,
                            challengeType = AlarmChallengeType.CAPTCHA,
                            challengeDifficulty = AlarmDifficulty.EASY,
                            volumePercent = 85,
                            isVibrationEnabled = true,
                            maxSnoozes = 2,
                            snoozeDurationMinutes = 5,
                            ringtonePreset = "ZEN_CHIME",
                            snoozeRingtonePreset = "BELL"
                        ),
                        AlarmEntity(
                            title = "Evening Deep Focus Session",
                            hour = 17,
                            minute = 30,
                            isEnabled = true,
                            challengeType = AlarmChallengeType.PHYSICAL_SHAKE,
                            challengeDifficulty = AlarmDifficulty.MEDIUM,
                            volumePercent = 90,
                            isVibrationEnabled = true,
                            maxSnoozes = 2,
                            snoozeDurationMinutes = 5,
                            ringtonePreset = "APEX_HORNS",
                            snoozeRingtonePreset = "TICK_TOCK"
                        )
                    )
                    defaultAlarms.forEach { rebuildRepository.saveAlarm(it) }
                } else {
                    _uiState.update { it.copy(alarms = alarmsList) }
                }
            }
        }

        viewModelScope.launch {
            rebuildRepository.getRecentAlarmLogs().collect { logs ->
                _uiState.update { it.copy(alarmLogs = logs) }
            }
        }
    }

    fun openCreateDialog() {
        stopSoundPreview()
        _uiState.update {
            it.copy(
                isCreatingOrEditing = true,
                editingAlarm = null,
                inputTitle = "Deep Work / Wake-Up Alarm",
                inputHour = 6,
                inputMinute = 0,
                inputChallengeType = AlarmChallengeType.MATH,
                inputDifficulty = AlarmDifficulty.MEDIUM,
                inputVolume = 90,
                inputVibrate = true,
                inputMaxSnoozes = 3,
                inputSnoozeDuration = 5,
                inputRingtonePreset = "CYBER_SIREN",
                inputSnoozeRingtonePreset = "TICK_TOCK",
                isPreviewingSound = false
            )
        }
    }

    fun openEditDialog(alarm: AlarmEntity) {
        stopSoundPreview()
        _uiState.update {
            it.copy(
                isCreatingOrEditing = true,
                editingAlarm = alarm,
                inputTitle = alarm.title,
                inputHour = alarm.hour,
                inputMinute = alarm.minute,
                inputChallengeType = alarm.challengeType,
                inputDifficulty = alarm.challengeDifficulty,
                inputVolume = alarm.volumePercent,
                inputVibrate = alarm.isVibrationEnabled,
                inputMaxSnoozes = alarm.maxSnoozes,
                inputSnoozeDuration = alarm.snoozeDurationMinutes,
                inputDaysOfWeek = alarm.repeatDaysJson,
                inputRingtonePreset = alarm.ringtonePreset,
                inputSnoozeRingtonePreset = alarm.snoozeRingtonePreset,
                isPreviewingSound = false
            )
        }
    }

    fun dismissDialog() {
        stopSoundPreview()
        _uiState.update { it.copy(isCreatingOrEditing = false, editingAlarm = null, isPreviewingSound = false) }
    }

    fun setInputTitle(title: String) = _uiState.update { it.copy(inputTitle = title) }
    fun setInputTime(hour: Int, minute: Int) = _uiState.update { it.copy(inputHour = hour, inputMinute = minute) }
    fun setInputChallengeType(type: AlarmChallengeType) = _uiState.update { it.copy(inputChallengeType = type) }
    fun setInputDifficulty(diff: AlarmDifficulty) = _uiState.update { it.copy(inputDifficulty = diff) }
    fun setInputVolume(vol: Int) = _uiState.update { it.copy(inputVolume = vol) }
    fun setInputVibrate(vibrate: Boolean) = _uiState.update { it.copy(inputVibrate = vibrate) }
    fun setInputMaxSnoozes(snoozes: Int) = _uiState.update { it.copy(inputMaxSnoozes = snoozes) }
    fun setInputSnoozeDuration(minutes: Int) = _uiState.update { it.copy(inputSnoozeDuration = minutes) }
    fun setInputRingtonePreset(preset: String) = _uiState.update { it.copy(inputRingtonePreset = preset) }
    fun setInputSnoozeRingtonePreset(preset: String) = _uiState.update { it.copy(inputSnoozeRingtonePreset = preset) }

    fun previewSound(context: Context, presetOrUri: String) {
        stopSoundPreview()
        try {
            var alertUri: Uri? = null
            if (presetOrUri.startsWith("content://") || presetOrUri.startsWith("file://") || presetOrUri.startsWith("android.resource://")) {
                try {
                    alertUri = Uri.parse(presetOrUri)
                } catch (e: Exception) {
                    alertUri = null
                }
            }
            if (alertUri == null) {
                alertUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                    ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                    ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
            }

            if (alertUri != null) {
                previewMediaPlayer = MediaPlayer().apply {
                    setDataSource(context.applicationContext, alertUri)
                    setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_ALARM)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                    )
                    isLooping = true
                    setVolume(0.85f, 0.85f)
                    prepare()
                    start()
                }
                _uiState.update { it.copy(isPreviewingSound = true, previewingToneName = presetOrUri) }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            _uiState.update { it.copy(isPreviewingSound = false) }
        }
    }

    fun stopSoundPreview() {
        try {
            previewMediaPlayer?.stop()
            previewMediaPlayer?.release()
            previewMediaPlayer = null
        } catch (e: Exception) {
            e.printStackTrace()
        }
        _uiState.update { it.copy(isPreviewingSound = false, previewingToneName = "") }
    }

    override fun onCleared() {
        super.onCleared()
        stopSoundPreview()
    }

    fun saveAlarm() {
        stopSoundPreview()
        val state = _uiState.value
        val entity = AlarmEntity(
            id = state.editingAlarm?.id ?: 0L,
            title = state.inputTitle.trim().ifEmpty { "Protocol Alarm" },
            hour = state.inputHour,
            minute = state.inputMinute,
            isEnabled = true,
            challengeType = state.inputChallengeType,
            challengeDifficulty = state.inputDifficulty,
            volumePercent = state.inputVolume,
            isVibrationEnabled = state.inputVibrate,
            maxSnoozes = state.inputMaxSnoozes,
            snoozeDurationMinutes = state.inputSnoozeDuration,
            repeatDaysJson = state.inputDaysOfWeek,
            ringtonePreset = state.inputRingtonePreset,
            snoozeRingtonePreset = state.inputSnoozeRingtonePreset
        )
        viewModelScope.launch {
            rebuildRepository.saveAlarm(entity)
            dismissDialog()
        }
    }

    fun toggleAlarm(alarm: AlarmEntity) {
        viewModelScope.launch {
            rebuildRepository.toggleAlarm(alarm)
        }
    }

    fun deleteAlarm(alarm: AlarmEntity) {
        viewModelScope.launch {
            rebuildRepository.deleteAlarm(alarm)
        }
    }
}

class AlarmsViewModelFactory(
    private val rebuildRepository: RebuildRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return AlarmsViewModel(rebuildRepository) as T
    }
}
