package com.example.viewmodel

import android.app.Activity
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.certificate.CertificateGeneratorEngine
import com.example.data.local.entity.LevelPurchaseEntity
import com.example.data.model.CertificateData
import com.example.data.model.RankLevel
import com.example.data.model.RankLevelSystem
import com.example.data.repository.RebuildRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

sealed interface ExportStatus {
    object Idle : ExportStatus
    object Exporting : ExportStatus
    data class Success(val message: String, val file: File, val mimeType: String) : ExportStatus
    data class Error(val message: String) : ExportStatus
}

data class MintSuccessData(
    val level: Int,
    val rankTitle: String,
    val xpDeducted: Int
)

data class CertificateUiState(
    val certificateData: CertificateData = CertificateData(),
    val availableLevels: List<Pair<Int, String>> = emptyList(),
    val unlockedLevels: Set<Int> = setOf(1),
    val mintedCertificates: Set<Int> = emptySet(),
    val levelPurchases: Map<Int, LevelPurchaseEntity> = emptyMap(),
    val currentXpBalance: Int = 0,
    val isEditMode: Boolean = false,
    val exportStatus: ExportStatus = ExportStatus.Idle,
    val liveProfileLoaded: Boolean = false,
    val selectedRankForMint: RankLevel? = null,
    val isMinting: Boolean = false,
    val selectedRankForUnlock: RankLevel? = null,
    val isUnlocking: Boolean = false,
    val mintSuccessData: MintSuccessData? = null,
    val showExportBottomSheet: Boolean = false,
    val exportSuccessToast: String? = null,
    val messageSnackbar: String? = null
)

class CertificateViewModel(
    private val repository: RebuildRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CertificateUiState())
    val uiState: StateFlow<CertificateUiState> = _uiState.asStateFlow()

    init {
        loadLevels()
        loadLiveProfileData()

        viewModelScope.launch {
            repository.getAllLevelPurchases().collectLatest { purchases ->
                val purchasesMap = purchases.associateBy { it.level }
                val unlocked = if (purchases.isEmpty()) setOf(1) else (purchases.map { it.level }.toSet() + 1)
                val minted = purchases.filter { it.isCertificateMinted }.map { it.level }.toSet()
                val xpBalance = repository.getCurrentXpBalance()

                _uiState.update { current ->
                    current.copy(
                        unlockedLevels = unlocked,
                        mintedCertificates = minted,
                        levelPurchases = purchasesMap,
                        currentXpBalance = xpBalance
                    )
                }
            }
        }
    }

    private fun loadLevels() {
        val levels = RankLevelSystem.RANKS.map { it.level to it.title }
        _uiState.update { it.copy(availableLevels = levels) }
    }

    fun openMintModal(rank: RankLevel) {
        _uiState.update { it.copy(selectedRankForMint = rank) }
    }

    fun dismissMintModal() {
        _uiState.update { it.copy(selectedRankForMint = null, isMinting = false) }
    }

    fun openUnlockModal(rank: RankLevel) {
        _uiState.update { it.copy(selectedRankForUnlock = rank) }
    }

    fun dismissUnlockModal() {
        _uiState.update { it.copy(selectedRankForUnlock = null, isUnlocking = false) }
    }

    fun confirmUnlockLevel(rank: RankLevel) {
        viewModelScope.launch {
            _uiState.update { it.copy(isUnlocking = true) }
            val res = repository.purchaseLevel(rank.level)
            res.onSuccess {
                val newBalance = repository.getCurrentXpBalance()
                _uiState.update { current ->
                    current.copy(
                        isUnlocking = false,
                        selectedRankForUnlock = null,
                        unlockedLevels = current.unlockedLevels + rank.level,
                        currentXpBalance = newBalance,
                        messageSnackbar = "Level ${rank.level} Unlocked!"
                    )
                }
                selectLevel(rank.level)
            }.onFailure { err ->
                _uiState.update {
                    it.copy(
                        isUnlocking = false,
                        selectedRankForUnlock = null,
                        messageSnackbar = err.message ?: "Failed to unlock level"
                    )
                }
            }
        }
    }

    fun clearSnackbar() {
        _uiState.update { it.copy(messageSnackbar = null) }
    }

    fun dismissMintSuccessData() {
        _uiState.update { it.copy(mintSuccessData = null) }
    }

    fun openExportSheet() {
        _uiState.update { it.copy(showExportBottomSheet = true) }
    }

    fun dismissExportSheet() {
        _uiState.update { it.copy(showExportBottomSheet = false) }
    }

    fun clearExportSuccessToast() {
        _uiState.update { it.copy(exportSuccessToast = null) }
    }

    fun confirmMintCertificate(rank: RankLevel) {
        viewModelScope.launch {
            _uiState.update { it.copy(isMinting = true) }
            val res = repository.mintCertificate(rank.level)
            res.onSuccess {
                val newBalance = repository.getCurrentXpBalance()
                _uiState.update {
                    it.copy(
                        isMinting = false,
                        selectedRankForMint = null,
                        currentXpBalance = newBalance,
                        mintedCertificates = it.mintedCertificates + rank.level,
                        mintSuccessData = MintSuccessData(
                            level = rank.level,
                            rankTitle = rank.title,
                            xpDeducted = rank.certificateCost
                        ),
                        messageSnackbar = "Certificate for Level ${rank.level} successfully generated!"
                    )
                }
            }.onFailure { err ->
                _uiState.update {
                    it.copy(
                        isMinting = false,
                        messageSnackbar = err.message ?: "Failed to generate certificate"
                    )
                }
            }
        }
    }

    fun loadLiveProfileData() {
        viewModelScope.launch {
            val userProfile = repository.getUserProfile().firstOrNull()
            val winterArc = repository.getWinterArcState().firstOrNull()

            val studentName = userProfile?.name?.ifBlank { "Kamlesh Kumar Thakur" } ?: "Kamlesh Kumar Thakur"
            val studentClass = "${userProfile?.studentClass ?: "Class 12"} • ${userProfile?.stream ?: "Science (PCM)"}"
            val xpVal = winterArc?.xp ?: 0
            val rank = RankLevelSystem.getRankForXp(xpVal)
            val currentLevel = rank.level
            val rankTitle = rank.title
            val arcDay = winterArc?.currentDay ?: 1
            val streak = winterArc?.streak ?: 0
            val todayStr = SimpleDateFormat("MMMM dd, yyyy", Locale.US).format(Date())

            val certId = CertificateData.generateCertificateId(currentLevel)
            val hash = CertificateData.generateVerificationHash(studentName, currentLevel, xpVal)
            val eval = CertificateData.defaultEvaluationForLevel(currentLevel, rankTitle)

            val liveData = CertificateData(
                studentName = studentName,
                studentClass = studentClass,
                winterArcDay = arcDay,
                level = currentLevel,
                rankTitle = rankTitle,
                xp = xpVal,
                streak = streak,
                dateAchieved = todayStr,
                certificateId = certId,
                verificationHash = hash,
                aiEvaluation = eval
            )

            _uiState.update {
                it.copy(
                    certificateData = liveData,
                    liveProfileLoaded = true
                )
            }
        }
    }

    fun selectLevel(newLevel: Int) {
        val rank = RankLevelSystem.RANKS.find { it.level == newLevel } ?: return
        _uiState.update { current ->
            val updatedCert = current.certificateData.copy(
                level = newLevel,
                rankTitle = rank.title,
                certificateId = CertificateData.generateCertificateId(newLevel),
                verificationHash = CertificateData.generateVerificationHash(
                    current.certificateData.studentName,
                    newLevel,
                    current.certificateData.xp
                ),
                aiEvaluation = CertificateData.defaultEvaluationForLevel(newLevel, rank.title)
            )
            current.copy(certificateData = updatedCert)
        }
    }

    fun updateStudentName(name: String) {
        _uiState.update { current ->
            val updated = current.certificateData.copy(
                studentName = name,
                verificationHash = CertificateData.generateVerificationHash(
                    name,
                    current.certificateData.level,
                    current.certificateData.xp
                )
            )
            current.copy(certificateData = updated)
        }
    }

    fun updateStudentClass(className: String) {
        _uiState.update { current ->
            current.copy(certificateData = current.certificateData.copy(studentClass = className))
        }
    }

    fun updateAiEvaluation(evaluation: String) {
        _uiState.update { current ->
            current.copy(certificateData = current.certificateData.copy(aiEvaluation = evaluation))
        }
    }

    fun updateDateAchieved(date: String) {
        _uiState.update { current ->
            current.copy(
                certificateData = current.certificateData.copy(
                    dateAchieved = date,
                    issueDate = date
                )
            )
        }
    }

    fun toggleEditMode() {
        _uiState.update { it.copy(isEditMode = !it.isEditMode) }
    }

    fun dismissExportStatus() {
        _uiState.update { it.copy(exportStatus = ExportStatus.Idle) }
    }

    fun exportJpg(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(exportStatus = ExportStatus.Exporting) }
            try {
                val file = CertificateGeneratorEngine.exportToJpg(context, _uiState.value.certificateData)
                _uiState.update {
                    it.copy(
                        exportStatus = ExportStatus.Success(
                            message = "JPG Certificate exported successfully!",
                            file = file,
                            mimeType = "image/jpeg"
                        )
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(exportStatus = ExportStatus.Error("Failed to export JPG: ${e.message}"))
                }
            }
        }
    }

    fun exportPng(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(exportStatus = ExportStatus.Exporting) }
            try {
                val file = CertificateGeneratorEngine.exportToPng(context, _uiState.value.certificateData)
                _uiState.update {
                    it.copy(
                        exportStatus = ExportStatus.Success(
                            message = "High-Res PNG Certificate exported successfully!",
                            file = file,
                            mimeType = "image/png"
                        )
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(exportStatus = ExportStatus.Error("Failed to export PNG: ${e.message}"))
                }
            }
        }
    }

    fun exportPdf(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(exportStatus = ExportStatus.Exporting) }
            try {
                val file = CertificateGeneratorEngine.exportToPdf(context, _uiState.value.certificateData)
                _uiState.update {
                    it.copy(
                        exportStatus = ExportStatus.Success(
                            message = "Official A4 Print-Ready PDF generated successfully!",
                            file = file,
                            mimeType = "application/pdf"
                        )
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(exportStatus = ExportStatus.Error("Failed to export PDF: ${e.message}"))
                }
            }
        }
    }

    fun exportFormat(context: Context, format: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(exportStatus = ExportStatus.Exporting) }
            try {
                val certData = _uiState.value.certificateData
                val upperFormat = format.uppercase(Locale.ROOT)
                val (file, mimeType, ext) = when (upperFormat) {
                    "PDF" -> Triple(
                        CertificateGeneratorEngine.exportToPdf(context, certData),
                        "application/pdf",
                        "pdf"
                    )
                    "PNG" -> Triple(
                        CertificateGeneratorEngine.exportToPng(context, certData),
                        "image/png",
                        "png"
                    )
                    else -> Triple(
                        CertificateGeneratorEngine.exportToJpg(context, certData),
                        "image/jpeg",
                        "jpg"
                    )
                }
                val savedFile = CertificateGeneratorEngine.saveFileToDeviceStorage(
                    context = context,
                    sourceFile = file,
                    mimeType = mimeType,
                    displayName = "REBUILD_Certificate_${certData.certificateId}.$ext"
                )
                _uiState.update {
                    it.copy(
                        showExportBottomSheet = false,
                        exportSuccessToast = "Certificate saved as $upperFormat",
                        exportStatus = ExportStatus.Success(
                            message = "Certificate saved as $upperFormat",
                            file = savedFile,
                            mimeType = mimeType
                        )
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        showExportBottomSheet = false,
                        exportStatus = ExportStatus.Error("Failed to save $format: ${e.message}")
                    )
                }
            }
        }
    }

    fun printCertificate(activity: Activity) {
        CertificateGeneratorEngine.printCertificate(activity, _uiState.value.certificateData)
    }

    fun shareCertificate(context: Context, file: File, mimeType: String) {
        CertificateGeneratorEngine.shareCertificate(context, file, mimeType)
    }
}

class CertificateViewModelFactory(
    private val repository: RebuildRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CertificateViewModel::class.java)) {
            return CertificateViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
