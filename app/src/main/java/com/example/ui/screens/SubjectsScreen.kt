package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Science
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.entity.ChapterEntity
import com.example.data.local.entity.SubjectEntity
import com.example.ui.components.FrostedGlassCard
import com.example.ui.components.GlowPill
import com.example.ui.components.HeroGlassCard
import com.example.ui.theme.DarkNavy
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.FrostBlueAccent
import com.example.ui.theme.FrostedNavyCard
import com.example.ui.theme.GlassWhite
import com.example.ui.theme.GlassWhiteMuted
import com.example.ui.theme.IceCyanPrimary
import com.example.ui.theme.PurpleArc
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.WarningAmber
import com.example.viewmodel.SubjectsViewModel

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.IconButton

@Composable
fun SubjectsScreen(
    viewModel: SubjectsViewModel,
    onStartFocusSession: (subject: String, chapter: String) -> Unit,
    onOpenDrawer: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 40.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onOpenDrawer,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(FrostedNavyCard)
                        .testTag("menu_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Menu,
                        contentDescription = "Open Navigation Menu",
                        tint = GlassWhite
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "ACADEMIC SYLLABUS",
                        style = MaterialTheme.typography.labelSmall,
                        color = IceCyanPrimary,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.2.sp
                    )
                    Text(
                        text = "Subjects & Chapter Engine",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                        color = GlassWhite
                    )
                }
            }
        }

        // Syllabus Overall Hero Card
        item {
            HeroGlassCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "TOTAL SYLLABUS MASTERY",
                            style = MaterialTheme.typography.labelSmall,
                            color = FrostBlueAccent,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${uiState.completedChaptersCount} of ${uiState.totalChaptersCount} Chapters",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Black,
                            color = GlassWhite
                        )
                        Text(
                            text = "Lectures • Notes • PYQs • Spaced Revisions",
                            style = MaterialTheme.typography.bodySmall,
                            color = GlassWhiteMuted,
                            fontSize = 11.sp
                        )
                    }

                    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(64.dp)) {
                        CircularProgressIndicator(
                            progress = { uiState.overallProgress / 100f },
                            modifier = Modifier.fillMaxSize(),
                            color = IceCyanPrimary,
                            trackColor = Color(0x3338E1FF),
                            strokeWidth = 6.dp,
                            strokeCap = StrokeCap.Round
                        )
                        Text(
                            text = "${uiState.overallProgress}%",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Black,
                            color = GlassWhite
                        )
                    }
                }
            }
        }

        // Subjects Tabs Horizontal Scroll
        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(vertical = 4.dp)
            ) {
                items(uiState.subjects, key = { it.id }) { subject ->
                    val isSelected = subject.id == uiState.selectedSubject?.id
                    val subColor = parseHexColor(subject.colorHex)

                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .clickable { viewModel.selectSubject(subject.id) },
                        shape = RoundedCornerShape(16.dp),
                        color = if (isSelected) subColor.copy(alpha = 0.25f) else Color(0x33102447),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isSelected) subColor else Color(0x205CE1E6)
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(subColor)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = subject.name,
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) GlassWhite else GlassWhiteMuted
                                )
                                Text(
                                    text = "${subject.completedChapters}/${subject.totalChapters} Ch",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = 10.sp,
                                    color = subColor
                                )
                            }
                        }
                    }
                }
            }
        }

        // Active Subject Chapters Title
        item {
            val activeName = uiState.selectedSubject?.name ?: "Physics"
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "$activeName Chapters",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = GlassWhite
                )
                Text(
                    text = "${uiState.chapters.size} Chapters Total",
                    style = MaterialTheme.typography.bodySmall,
                    color = GlassWhiteMuted
                )
            }
        }

        // Chapters List
        items(uiState.chapters, key = { it.id }) { chapter ->
            ChapterProgressCard(
                chapter = chapter,
                subjectName = uiState.selectedSubject?.name ?: "Physics",
                onUpdate = { viewModel.updateChapterProgress(it) },
                onIncrementRevision = { viewModel.incrementRevision(chapter.id) },
                onFocusChapter = {
                    onStartFocusSession(uiState.selectedSubject?.name ?: "Physics", chapter.title)
                }
            )
        }
    }
}

@Composable
fun ChapterProgressCard(
    chapter: ChapterEntity,
    subjectName: String,
    onUpdate: (ChapterEntity) -> Unit,
    onIncrementRevision: () -> Unit,
    onFocusChapter: () -> Unit,
    modifier: Modifier = Modifier
) {
    FrostedGlassCard(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "CHAPTER ${chapter.chapterNumber}",
                        style = MaterialTheme.typography.labelSmall,
                        color = FrostBlueAccent,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = chapter.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = GlassWhite
                    )
                }

                GlowPill(
                    text = "${chapter.completionPercentage}%",
                    color = if (chapter.isCompleted) SuccessGreen else IceCyanPrimary
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Progress bar
            LinearProgressIndicator(
                progress = { chapter.completionPercentage / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(5.dp)
                    .clip(CircleShape),
                color = if (chapter.isCompleted) SuccessGreen else IceCyanPrimary,
                trackColor = Color(0x331F3A60),
                strokeCap = StrokeCap.Round
            )

            Spacer(modifier = Modifier.height(14.dp))

            // 4 Components: Lectures, Notes, PYQs, Revision
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Lectures Counter
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0x33102447),
                    border = androidx.compose.foundation.BorderStroke(1.dp, FrostBlueAccent.copy(alpha = 0.3f)),
                    modifier = Modifier.clickable {
                        val next = if (chapter.completedLectures < chapter.totalLectures) chapter.completedLectures + 1 else 0
                        onUpdate(chapter.copy(completedLectures = next))
                    }
                ) {
                    Text(
                        text = "Lectures: ${chapter.completedLectures}/${chapter.totalLectures}",
                        style = MaterialTheme.typography.labelSmall,
                        color = GlassWhite,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                    )
                }

                // Notes Checkbox
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { onUpdate(chapter.copy(notesDone = !chapter.notesDone)) }
                ) {
                    Icon(
                        imageVector = if (chapter.notesDone) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                        contentDescription = "Notes",
                        tint = if (chapter.notesDone) SuccessGreen else GlassWhiteMuted,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Notes", style = MaterialTheme.typography.labelSmall, color = GlassWhite)
                }

                // PYQ Checkbox
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { onUpdate(chapter.copy(pyqsDone = !chapter.pyqsDone)) }
                ) {
                    Icon(
                        imageVector = if (chapter.pyqsDone) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                        contentDescription = "PYQs",
                        tint = if (chapter.pyqsDone) SuccessGreen else GlassWhiteMuted,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("PYQs", style = MaterialTheme.typography.labelSmall, color = GlassWhite)
                }

                // Revision Count & +1 button
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0x3338E1FF),
                    border = androidx.compose.foundation.BorderStroke(1.dp, IceCyanPrimary),
                    modifier = Modifier.clickable { onIncrementRevision() }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Rev: ${chapter.revisionCount}",
                            style = MaterialTheme.typography.labelSmall,
                            color = GlassWhite,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Revise",
                            tint = IceCyanPrimary,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }
    }
}

fun parseHexColor(hex: String): Color {
    return try {
        Color(android.graphics.Color.parseColor(hex))
    } catch (e: Exception) {
        IceCyanPrimary
    }
}
