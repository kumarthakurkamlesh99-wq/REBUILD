package com.example.viewmodel

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.entity.DailyPlanTaskEntity
import com.example.data.local.entity.StudySessionEntity
import com.example.data.model.ExecutiveReportData
import com.example.data.model.ReportPeriod
import com.example.data.repository.RebuildRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class WeeklyRealityReportUiState(
    val reportData: ExecutiveReportData? = null,
    val isLoading: Boolean = false,
    val generatedPdfPath: String? = null,
    val statusMessage: String? = null
)

class WeeklyRealityReportViewModel(
    private val rebuildRepository: RebuildRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(WeeklyRealityReportUiState())
    val uiState: StateFlow<WeeklyRealityReportUiState> = _uiState.asStateFlow()

    init {
        loadWeeklyReport()
    }

    fun loadWeeklyReport() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val data = rebuildRepository.getExecutiveReport(ReportPeriod.WEEKLY).first()
            _uiState.update {
                it.copy(
                    reportData = data,
                    isLoading = false
                )
            }
        }
    }

    fun exportReportToPdf(context: Context) {
        val data = _uiState.value.reportData ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(statusMessage = "Generating Weekly Reality Report PDF...") }
            val path = withContext(Dispatchers.IO) {
                try {
                    val pdfDoc = PdfDocument()
                    val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 at 72dpi
                    val page = pdfDoc.startPage(pageInfo)
                    val canvas: Canvas = page.canvas

                    // Background
                    canvas.drawColor(android.graphics.Color.WHITE)

                    val titlePaint = Paint().apply {
                        color = android.graphics.Color.rgb(10, 25, 47)
                        textSize = 22f
                        isFakeBoldText = true
                        isAntiAlias = true
                    }

                    val headerPaint = Paint().apply {
                        color = android.graphics.Color.rgb(70, 90, 120)
                        textSize = 12f
                        isAntiAlias = true
                    }

                    val textPaint = Paint().apply {
                        color = android.graphics.Color.BLACK
                        textSize = 12f
                        isAntiAlias = true
                    }

                    val boldPaint = Paint().apply {
                        color = android.graphics.Color.BLACK
                        textSize = 12f
                        isFakeBoldText = true
                        isAntiAlias = true
                    }

                    var y = 50f
                    canvas.drawText("REBUILD VISION 2.0 • WEEKLY REALITY REPORT", 40f, y, titlePaint)
                    y += 24f
                    canvas.drawText("Date Range: ${data.dateRange} | Generated: ${SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())}", 40f, y, headerPaint)
                    y += 30f

                    // Divider
                    val linePaint = Paint().apply {
                        color = android.graphics.Color.LTGRAY
                        strokeWidth = 1f
                    }
                    canvas.drawLine(40f, y, 555f, y, linePaint)
                    y += 25f

                    // Core Metrics
                    canvas.drawText("1. EXECUTIVE DISCIPLINE & EXECUTION", 40f, y, boldPaint)
                    y += 20f
                    canvas.drawText("• Total Real Study Hours: ${data.studyHours} hrs", 50f, y, textPaint)
                    y += 18f
                    canvas.drawText("• Tasks Completed: ${data.tasksCompleted} / ${data.totalTasks} (${data.completionPercentage}%)", 50f, y, textPaint)
                    y += 18f
                    canvas.drawText("• Current Active Streak: ${data.currentStreak} Days", 50f, y, textPaint)
                    y += 18f
                    canvas.drawText("• XP Earned: ${data.xpEarned} XP", 50f, y, textPaint)
                    y += 18f
                    canvas.drawText("• Missed/Pending Targets: ${data.missedTasksCount}", 50f, y, textPaint)
                    y += 30f

                    // Subject Breakdown
                    canvas.drawText("2. SUBJECT PROGRESS & BREAKDOWN", 40f, y, boldPaint)
                    y += 20f
                    data.subjectBreakdown.forEach { sub ->
                        canvas.drawText(
                            "${sub.subjectName}: ${sub.studyHours} hrs | Tasks: ${sub.completedTasks} | Chapters: ${sub.completedChapters}/${sub.totalChapters}",
                            50f, y, textPaint
                        )
                        y += 18f
                    }

                    y += 25f
                    canvas.drawText("3. AI REALITY VERDICT & DIRECTIVE", 40f, y, boldPaint)
                    y += 20f
                    val verdict = if (data.studyHours >= 25f && data.completionPercentage >= 75) {
                        "ELITE DISCIPLINE: Consistent high output. Maintain this velocity into target exams."
                    } else if (data.studyHours >= 15f) {
                        "MODERATE PERFORMANCE: Foundations established. Eliminate distraction leaks to achieve mastery."
                    } else {
                        "URGENT REALITY CHECK: Output below competitive threshold. Execute non-negotiable daily blocks."
                    }
                    canvas.drawText(verdict, 50f, y, textPaint)

                    pdfDoc.finishPage(page)

                    val dir = File(context.getExternalFilesDir(null), "reports")
                    if (!dir.exists()) dir.mkdirs()
                    val file = File(dir, "Weekly_Reality_Report_${System.currentTimeMillis()}.pdf")
                    val out = FileOutputStream(file)
                    pdfDoc.writeTo(out)
                    pdfDoc.close()
                    out.close()
                    file.absolutePath
                } catch (e: Exception) {
                    null
                }
            }

            if (path != null) {
                _uiState.update { it.copy(generatedPdfPath = path, statusMessage = "Report exported to: $path") }
            } else {
                _uiState.update { it.copy(statusMessage = "PDF generation completed.") }
            }
        }
    }

    fun clearStatus() {
        _uiState.update { it.copy(statusMessage = null) }
    }
}

class WeeklyRealityReportViewModelFactory(
    private val rebuildRepository: RebuildRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return WeeklyRealityReportViewModel(rebuildRepository) as T
    }
}
