package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.entity.SyllabusChapterEntity
import com.example.data.local.entity.SyllabusStatus
import com.example.data.local.entity.SyllabusTopicEntity
import com.example.data.local.entity.SyllabusUnitEntity
import com.example.ui.components.RebuildTopAppBar
import com.example.ui.theme.DarkNavy
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.FireOrange
import com.example.ui.theme.FrostBlueAccent
import com.example.ui.theme.FrostedNavyCard
import com.example.ui.theme.GlassWhite
import com.example.ui.theme.GlassWhiteMuted
import com.example.ui.theme.IceCyanPrimary
import com.example.ui.theme.PurpleArc
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.WarningAmber
import com.example.viewmodel.SyllabusViewModel

@Composable
fun SyllabusScreen(
    viewModel: SyllabusViewModel,
    onOpenDrawer: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val currentSummary = uiState.subjectSummaries.find { it.code == uiState.selectedSubjectCode }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkNavy)
    ) {
        RebuildTopAppBar(
            title = "Class 12 Syllabus Tracker",
            subtitle = "Mastery Hierarchy: Subject → Unit → Chapter → Topic",
            onMenuClick = onOpenDrawer
        )

        // Subject Switcher Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val subjectList = listOf(
                "PHYSICS" to "Physics (14 Ch)",
                "CHEMISTRY" to "Chemistry (10 Ch)",
                "BIOLOGY" to "Biology (13 Ch)",
                "HINDI" to "Hindi Core (17 Ch)",
                "ENGLISH" to "English Core (16 Ch)"
            )

            subjectList.forEach { (code, label) ->
                val isSelected = uiState.selectedSubjectCode == code
                val subSummary = uiState.subjectSummaries.find { it.code == code }
                val pct = subSummary?.percentage ?: 0

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .background(if (isSelected) ElectricBlue.copy(alpha = 0.25f) else FrostedNavyCard)
                        .border(
                            1.dp,
                            if (isSelected) ElectricBlue else FrostBlueAccent.copy(alpha = 0.2f),
                            RoundedCornerShape(14.dp)
                        )
                        .clickable { viewModel.selectSubject(code) }
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = label,
                        fontSize = 13.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) IceCyanPrimary else GlassWhite
                    )
                    Text(
                        text = "$pct%",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (pct >= 80) SuccessGreen else if (pct > 0) FrostBlueAccent else GlassWhiteMuted
                    )
                }
            }
        }

        // Subject Analytics Hero Card
        if (currentSummary != null) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = FrostedNavyCard),
                border = BorderStroke(1.dp, FrostBlueAccent.copy(alpha = 0.3f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "${currentSummary.name} Mastery",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = GlassWhite
                            )
                            Text(
                                text = "${currentSummary.completedChapters} of ${currentSummary.totalChapters} Chapters Completed (${currentSummary.masteredChapters} Mastered)",
                                fontSize = 12.sp,
                                color = FrostBlueAccent
                            )
                        }

                        Text(
                            text = "${currentSummary.percentage}%",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black,
                            color = IceCyanPrimary
                        )
                    }

                    LinearProgressIndicator(
                        progress = { (currentSummary.percentage / 100f).coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = IceCyanPrimary,
                        trackColor = Color(0xFF0F172A)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Topics: ${currentSummary.completedTopics}/${currentSummary.totalTopics}",
                            fontSize = 11.sp,
                            color = GlassWhiteMuted
                        )
                        Text(
                            text = "CBSE Class 12 Syllabus Matrix",
                            fontSize = 11.sp,
                            color = GlassWhiteMuted
                        )
                    }
                }
            }
        }

        // Search Bar
        OutlinedTextField(
            value = uiState.searchQuery,
            onValueChange = { viewModel.setSearchQuery(it) },
            placeholder = { Text("Search units, chapters, or topics...", color = GlassWhiteMuted, fontSize = 13.sp) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = FrostBlueAccent) },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp)
                .testTag("syllabus_search_input"),
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = GlassWhite,
                unfocusedTextColor = GlassWhite,
                focusedBorderColor = IceCyanPrimary,
                unfocusedBorderColor = FrostBlueAccent.copy(alpha = 0.25f),
                focusedContainerColor = FrostedNavyCard,
                unfocusedContainerColor = FrostedNavyCard
            )
        )

        // Hierarchy List: Units -> Chapters -> Topics
        val filteredUnits = if (uiState.searchQuery.isBlank()) {
            uiState.units
        } else {
            uiState.units.filter { unit ->
                unit.unitTitle.contains(uiState.searchQuery, ignoreCase = true) ||
                uiState.chapters.any { it.unitId == unit.id && it.title.contains(uiState.searchQuery, ignoreCase = true) } ||
                uiState.topics.any { it.unitId == unit.id && it.title.contains(uiState.searchQuery, ignoreCase = true) }
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(filteredUnits, key = { it.id }) { unit ->
                UnitAccordionItem(
                    unit = unit,
                    allChapters = uiState.chapters.filter { it.unitId == unit.id },
                    allTopics = uiState.topics.filter { it.unitId == unit.id },
                    isExpanded = uiState.expandedUnitId == unit.id,
                    expandedChapterId = uiState.expandedChapterId,
                    onToggleUnit = { viewModel.toggleUnitExpanded(unit.id) },
                    onToggleChapter = { chId -> viewModel.toggleChapterExpanded(chId) },
                    onUpdateChapterStatus = { chId, status -> viewModel.updateChapterStatus(chId, status) },
                    onUpdateTopicStatus = { topId, status -> viewModel.updateTopicStatus(topId, status) },
                    onToggleChapterNotes = { ch -> viewModel.toggleChapterNotes(ch) }
                )
            }
        }
    }
}

@Composable
fun UnitAccordionItem(
    unit: SyllabusUnitEntity,
    allChapters: List<SyllabusChapterEntity>,
    allTopics: List<SyllabusTopicEntity>,
    isExpanded: Boolean,
    expandedChapterId: Long?,
    onToggleUnit: () -> Unit,
    onToggleChapter: (Long) -> Unit,
    onUpdateChapterStatus: (Long, SyllabusStatus) -> Unit,
    onUpdateTopicStatus: (Long, SyllabusStatus) -> Unit,
    onToggleChapterNotes: (SyllabusChapterEntity) -> Unit
) {
    val rotationState by animateFloatAsState(targetValue = if (isExpanded) 180f else 0f, label = "rot")

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = FrostedNavyCard),
        border = BorderStroke(1.dp, if (isExpanded) ElectricBlue.copy(alpha = 0.5f) else FrostBlueAccent.copy(alpha = 0.2f))
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Unit Header Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggleUnit() }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(ElectricBlue.copy(alpha = 0.2f))
                            .border(1.dp, ElectricBlue, CircleShape)
                    ) {
                        Text(
                            text = "U${unit.unitNumber}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = IceCyanPrimary
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = unit.unitTitle,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = GlassWhite,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${allChapters.size} Chapters • ${unit.completedTopicsCount}/${unit.totalTopicsCount} Topics (${unit.completionPercentage}%)",
                            fontSize = 11.sp,
                            color = FrostBlueAccent
                        )
                    }
                }

                Icon(
                    imageVector = Icons.Default.ExpandMore,
                    contentDescription = "Expand Unit",
                    tint = FrostBlueAccent,
                    modifier = Modifier.rotate(rotationState)
                )
            }

            // Chapters in Unit
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    allChapters.forEach { chapter ->
                        ChapterCardItem(
                            chapter = chapter,
                            topics = allTopics.filter { it.chapterId == chapter.id },
                            isExpanded = expandedChapterId == chapter.id,
                            onToggleChapter = { onToggleChapter(chapter.id) },
                            onUpdateStatus = { st -> onUpdateChapterStatus(chapter.id, st) },
                            onUpdateTopicStatus = onUpdateTopicStatus,
                            onToggleNotes = { onToggleChapterNotes(chapter) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ChapterCardItem(
    chapter: SyllabusChapterEntity,
    topics: List<SyllabusTopicEntity>,
    isExpanded: Boolean,
    onToggleChapter: () -> Unit,
    onUpdateStatus: (SyllabusStatus) -> Unit,
    onUpdateTopicStatus: (Long, SyllabusStatus) -> Unit,
    onToggleNotes: () -> Unit
) {
    val rotationState by animateFloatAsState(targetValue = if (isExpanded) 180f else 0f, label = "rotCh")
    var showStatusMenu by remember { mutableStateOf(false) }

    val statusColor = when (chapter.status) {
        SyllabusStatus.MASTERED -> PurpleArc
        SyllabusStatus.COMPLETED -> SuccessGreen
        SyllabusStatus.REVISED_ONCE, SyllabusStatus.REVISED_TWICE -> ElectricBlue
        SyllabusStatus.IN_PROGRESS -> WarningAmber
        SyllabusStatus.NOT_STARTED -> GlassWhiteMuted
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0C1628)),
        border = BorderStroke(1.dp, statusColor.copy(alpha = 0.35f))
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggleChapter() }
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "Ch ${chapter.chapterNumber}:",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = FrostBlueAccent
                        )
                        Text(
                            text = chapter.title,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = GlassWhite,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Status Badge with Dropdown Trigger
                        Box {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(statusColor.copy(alpha = 0.2f))
                                    .border(1.dp, statusColor, RoundedCornerShape(8.dp))
                                    .clickable { showStatusMenu = true }
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = chapter.status.label,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = statusColor
                                )
                            }

                            DropdownMenu(
                                expanded = showStatusMenu,
                                onDismissRequest = { showStatusMenu = false },
                                modifier = Modifier.background(FrostedNavyCard)
                            ) {
                                SyllabusStatus.values().forEach { st ->
                                    DropdownMenuItem(
                                        text = { Text(st.label, color = GlassWhite, fontSize = 12.sp) },
                                        onClick = {
                                            onUpdateStatus(st)
                                            showStatusMenu = false
                                        }
                                    )
                                }
                            }
                        }

                        Text(
                            text = "${chapter.completedTopicsCount}/${chapter.totalTopicsCount} topics",
                            fontSize = 11.sp,
                            color = GlassWhiteMuted
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.ExpandMore,
                        contentDescription = "Expand Chapter",
                        tint = FrostBlueAccent,
                        modifier = Modifier.rotate(rotationState)
                    )
                }
            }

            // Topics Breakdown & Checklist
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF070E1A))
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Quick Action Badges: Notes & PYQs
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (chapter.notesDone) SuccessGreen.copy(alpha = 0.2f) else Color(0xFF131F37))
                                .border(1.dp, if (chapter.notesDone) SuccessGreen else FrostBlueAccent.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                                .clickable { onToggleNotes() }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = if (chapter.notesDone) Icons.Default.CheckCircle else Icons.Default.Description,
                                contentDescription = null,
                                tint = if (chapter.notesDone) SuccessGreen else FrostBlueAccent,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (chapter.notesDone) "Notes Done" else "Mark Notes Done",
                                fontSize = 11.sp,
                                color = if (chapter.notesDone) SuccessGreen else GlassWhite
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (chapter.pyqsDone) SuccessGreen.copy(alpha = 0.2f) else Color(0xFF131F37))
                                .border(1.dp, if (chapter.pyqsDone) SuccessGreen else FrostBlueAccent.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                                .clickable {
                                    val newStatus = if (chapter.status == SyllabusStatus.MASTERED) SyllabusStatus.COMPLETED else SyllabusStatus.MASTERED
                                    onUpdateStatus(newStatus)
                                }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.WorkspacePremium,
                                contentDescription = null,
                                tint = if (chapter.status == SyllabusStatus.MASTERED) PurpleArc else ElectricBlue,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (chapter.status == SyllabusStatus.MASTERED) "Mastered (100 XP)" else "Mark Mastered",
                                fontSize = 11.sp,
                                color = if (chapter.status == SyllabusStatus.MASTERED) PurpleArc else GlassWhite
                            )
                        }
                    }

                    Text(
                        text = "TOPICS IN THIS CHAPTER:",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = FrostBlueAccent,
                        letterSpacing = 1.sp
                    )

                    topics.forEach { topic ->
                        TopicRowItem(
                            topic = topic,
                            onStatusChange = { newSt -> onUpdateTopicStatus(topic.id, newSt) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TopicRowItem(
    topic: SyllabusTopicEntity,
    onStatusChange: (SyllabusStatus) -> Unit
) {
    val isCompleted = topic.status == SyllabusStatus.COMPLETED || topic.status == SyllabusStatus.MASTERED || topic.status == SyllabusStatus.REVISED_ONCE || topic.status == SyllabusStatus.REVISED_TWICE
    var showMenu by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF0F1A2E))
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            IconButton(
                onClick = {
                    val next = if (isCompleted) SyllabusStatus.NOT_STARTED else SyllabusStatus.COMPLETED
                    onStatusChange(next)
                },
                modifier = Modifier.size(28.dp)
            ) {
                Icon(
                    imageVector = if (isCompleted) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                    contentDescription = "Toggle Topic",
                    tint = if (isCompleted) SuccessGreen else GlassWhiteMuted,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(6.dp))

            Text(
                text = "${topic.topicNumber}. ${topic.title}",
                fontSize = 12.sp,
                fontWeight = if (isCompleted) FontWeight.SemiBold else FontWeight.Normal,
                color = if (isCompleted) GlassWhite else GlassWhiteMuted
            )
        }

        Box {
            Text(
                text = topic.status.label,
                fontSize = 10.sp,
                color = when (topic.status) {
                    SyllabusStatus.MASTERED -> PurpleArc
                    SyllabusStatus.COMPLETED -> SuccessGreen
                    SyllabusStatus.REVISED_ONCE, SyllabusStatus.REVISED_TWICE -> ElectricBlue
                    SyllabusStatus.IN_PROGRESS -> WarningAmber
                    SyllabusStatus.NOT_STARTED -> GlassWhiteMuted
                },
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0xFF090F1C))
                    .clickable { showMenu = true }
                    .padding(horizontal = 6.dp, vertical = 3.dp)
            )

            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = { showMenu = false },
                modifier = Modifier.background(FrostedNavyCard)
            ) {
                SyllabusStatus.values().forEach { st ->
                    DropdownMenuItem(
                        text = { Text(st.label, color = GlassWhite, fontSize = 11.sp) },
                        onClick = {
                            onStatusChange(st)
                            showMenu = false
                        }
                    )
                }
            }
        }
    }
}
