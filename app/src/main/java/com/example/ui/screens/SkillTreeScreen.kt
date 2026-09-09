package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.entity.SkillNodeEntity
import com.example.ui.components.FrostedGlassCard
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
import com.example.viewmodel.SkillTreeViewModel

@Composable
fun SkillTreeScreen(
    viewModel: SkillTreeViewModel,
    onOpenDrawer: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkNavy)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            RebuildTopAppBar(
                title = "Skill Progression Trees",
                subtitle = "Mastery Archetypes • Tier Unlock System",
                onMenuClick = onOpenDrawer
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(top = 12.dp, bottom = 48.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Category Switcher
                item {
                    SkillCategorySelector(
                        selectedCategory = uiState.selectedCategory,
                        onSelect = { viewModel.selectCategory(it) }
                    )
                }

                // Overall Stats Header
                item {
                    SkillTreeOverviewCard(
                        category = uiState.selectedCategory,
                        masteredCount = uiState.totalMastered,
                        totalCount = uiState.totalNodes,
                        xpEarned = uiState.totalXpEarned
                    )
                }

                // Progression Nodes
                items(uiState.nodes, key = { it.id }) { node ->
                    SkillNodeCard(
                        node = node,
                        onProgressChange = { percent ->
                            viewModel.updateNodeMastery(node, percent)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun SkillCategorySelector(
    selectedCategory: String,
    onSelect: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(FrostedNavyCard)
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        val categories = listOf(
            Triple("CODING", "Coding & Tech", Icons.Default.Code),
            Triple("FITNESS", "Strength & Body", Icons.Default.FitnessCenter),
            Triple("EXAMS", "Exams & Academics", Icons.Default.School)
        )

        categories.forEach { (catKey, label, icon) ->
            val isSelected = selectedCategory == catKey
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (isSelected) ElectricBlue else Color.Transparent)
                    .clickable { onSelect(catKey) }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (isSelected) GlassWhite else GlassWhiteMuted,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = label,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) GlassWhite else GlassWhiteMuted
                    )
                }
            }
        }
    }
}

@Composable
private fun SkillTreeOverviewCard(
    category: String,
    masteredCount: Int,
    totalCount: Int,
    xpEarned: Int
) {
    FrostedGlassCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "$category MASTERY TREE",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = FrostBlueAccent,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "$masteredCount / $totalCount Tiers Mastered",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = GlassWhite
                    )
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = FireOrange.copy(alpha = 0.2f),
                    border = BorderStroke(1.dp, FireOrange.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Bolt,
                            contentDescription = null,
                            tint = FireOrange,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "+$xpEarned XP",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = FireOrange
                        )
                    }
                }
            }

            val progress = if (totalCount > 0) masteredCount.toFloat() / totalCount else 0f
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(CircleShape),
                color = IceCyanPrimary,
                trackColor = Color(0x33284B75)
            )
        }
    }
}

@Composable
private fun SkillNodeCard(
    node: SkillNodeEntity,
    onProgressChange: (Int) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = FrostedNavyCard),
        border = BorderStroke(
            1.dp,
            if (node.isMastered) SuccessGreen.copy(alpha = 0.6f)
            else if (node.isUnlocked) IceCyanPrimary.copy(alpha = 0.3f)
            else Color.White.copy(alpha = 0.08f)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Surface(
                        modifier = Modifier.size(36.dp),
                        shape = CircleShape,
                        color = if (node.isMastered) SuccessGreen.copy(alpha = 0.2f)
                        else if (node.isUnlocked) ElectricBlue.copy(alpha = 0.3f)
                        else Color(0x22102030)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = if (node.isMastered) Icons.Default.CheckCircle
                                else if (node.isUnlocked) Icons.Default.AutoAwesome
                                else Icons.Default.Lock,
                                contentDescription = null,
                                tint = if (node.isMastered) SuccessGreen
                                else if (node.isUnlocked) IceCyanPrimary
                                else GlassWhiteMuted,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "TIER ${node.tierLevel}",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (node.isMastered) SuccessGreen else IceCyanPrimary
                            )
                            if (node.isMastered) {
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = SuccessGreen.copy(alpha = 0.2f)
                                ) {
                                    Text(
                                        text = "MASTERED",
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = SuccessGreen,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                        Text(
                            text = node.nodeName,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = GlassWhite
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0x203498DB),
                    modifier = Modifier.clickable { expanded = !expanded }
                ) {
                    Text(
                        text = "${node.masteryPercentage}%",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black,
                        color = if (node.isMastered) SuccessGreen else IceCyanPrimary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            if (node.description.isNotBlank()) {
                Text(
                    text = node.description,
                    fontSize = 12.sp,
                    color = GlassWhiteMuted,
                    lineHeight = 16.sp
                )
            }

            // Interactive Progress Slider when expanded
            AnimatedVisibility(visible = expanded || node.isUnlocked) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Calibrate Mastery", fontSize = 11.sp, color = FrostBlueAccent)
                        Text("+${node.xpReward} XP upon 100%", fontSize = 10.sp, color = FireOrange)
                    }

                    Slider(
                        value = node.masteryPercentage.toFloat(),
                        onValueChange = { onProgressChange(it.toInt()) },
                        valueRange = 0f..100f,
                        steps = 19,
                        colors = SliderDefaults.colors(
                            thumbColor = IceCyanPrimary,
                            activeTrackColor = IceCyanPrimary,
                            inactiveTrackColor = Color(0x33284B75)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}
