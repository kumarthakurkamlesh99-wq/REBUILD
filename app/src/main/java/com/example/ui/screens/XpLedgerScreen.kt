package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Style
import androidx.compose.material.icons.filled.Today
import androidx.compose.material.icons.filled.ViewWeek
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.entity.XpTransactionEntity
import com.example.ui.theme.DarkLuxuryBackground
import com.example.ui.theme.DangerRed
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.FireOrange
import com.example.ui.theme.FrostedNavyCard
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.GlassWhite
import com.example.ui.theme.GlassWhiteMuted
import com.example.ui.theme.GlowBorderBrush
import com.example.ui.theme.HeroCardGradient
import com.example.ui.theme.IceCyanGlow
import com.example.ui.theme.IceCyanPrimary
import com.example.ui.theme.LuxuryAccent
import com.example.ui.theme.LuxuryCard
import com.example.ui.theme.PurpleArc
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.WarningAmber
import com.example.viewmodel.XpLedgerViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun XpLedgerScreen(
    viewModel: XpLedgerViewModel,
    onNavigateBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val focusManager = LocalFocusManager.current
    var showQuickLogDialog by remember { mutableStateOf(false) }

    val categories = listOf(
        "All",
        "Study",
        "Workout",
        "Discipline",
        "Habit",
        "School",
        "Revision"
    )

    Scaffold(
        modifier = Modifier.testTag("xp_ledger_screen"),
        containerColor = DarkLuxuryBackground,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "XP Ledger",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = GlassWhite
                        )
                        Text(
                            text = "Decentralized Discipline Telemetry",
                            style = MaterialTheme.typography.labelSmall,
                            color = IceCyanPrimary
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("xp_ledger_back_btn")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = GlassWhite
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showQuickLogDialog = !showQuickLogDialog },
                        modifier = Modifier.testTag("xp_quick_log_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Quick Log XP",
                            tint = IceCyanPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkLuxuryBackground
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. XP Telemetry Summary Grid (Daily, Weekly, Monthly, Total)
            item {
                XpMetricsGrid(
                    dailyXp = state.summary.dailyXp,
                    weeklyXp = state.summary.weeklyXp,
                    monthlyXp = state.summary.monthlyXp,
                    totalXp = state.summary.totalXp
                )
            }

            // Quick XP Action Sheet (Collapsible)
            item {
                AnimatedVisibility(visible = showQuickLogDialog) {
                    QuickXpLoggingCard(
                        onLog = { title, cat, xp ->
                            viewModel.logQuickXp(title, cat, xp)
                            showQuickLogDialog = false
                        }
                    )
                }
            }

            // 2. Search Field
            item {
                OutlinedTextField(
                    value = state.searchQuery,
                    onValueChange = { viewModel.updateSearchQuery(it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("xp_search_input"),
                    placeholder = {
                        Text(
                            text = "Search transactions, subjects, habits...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = GlassWhiteMuted
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = IceCyanPrimary
                        )
                    },
                    trailingIcon = {
                        if (state.searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.updateSearchQuery("") }) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Clear",
                                    tint = GlassWhiteMuted
                                )
                            }
                        }
                    },
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = IceCyanPrimary,
                        unfocusedBorderColor = GlassBorder,
                        focusedContainerColor = FrostedNavyCard,
                        unfocusedContainerColor = Color(0xFF070B16),
                        focusedTextColor = GlassWhite,
                        unfocusedTextColor = GlassWhite
                    ),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() })
                )
            }

            // 3. Category Filter Chips (Horizontal Scroll)
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    categories.forEach { category ->
                        val isSelected = state.selectedCategory.equals(category, ignoreCase = true)
                        val chipBg by animateColorAsState(
                            targetValue = if (isSelected) IceCyanPrimary else Color(0xFF0E1629),
                            label = "chip_bg"
                        )
                        val textColor by animateColorAsState(
                            targetValue = if (isSelected) Color(0xFF050816) else GlassWhiteMuted,
                            label = "chip_text"
                        )

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(chipBg)
                                .border(
                                    width = 1.dp,
                                    color = if (isSelected) IceCyanPrimary else GlassBorder,
                                    shape = RoundedCornerShape(10.dp)
                                )
                                .clickable { viewModel.selectCategory(category) }
                                .padding(horizontal = 14.dp, vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = category,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = textColor
                            )
                        }
                    }
                }
            }

            // 4. Section Subtitle
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "TRANSACTION TIMELINE (${state.filteredTransactions.size})",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = GlassWhiteMuted,
                        letterSpacing = 1.sp
                    )
                    if (state.selectedCategory != "All") {
                        Text(
                            text = "Filter: ${state.selectedCategory}",
                            style = MaterialTheme.typography.labelSmall,
                            color = IceCyanPrimary
                        )
                    }
                }
            }

            // 5. Timeline Transactions List
            if (state.isLoading) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = IceCyanPrimary)
                    }
                }
            } else if (state.filteredTransactions.isEmpty()) {
                item {
                    EmptyTransactionsCard(
                        query = state.searchQuery,
                        category = state.selectedCategory,
                        onQuickLog = { showQuickLogDialog = true }
                    )
                }
            } else {
                items(state.filteredTransactions, key = { it.id }) { tx ->
                    XpTransactionItemCard(tx = tx)
                }
            }

            item {
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
fun XpMetricsGrid(
    dailyXp: Int,
    weeklyXp: Int,
    monthlyXp: Int,
    totalXp: Int
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, GlowBorderBrush, RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = LuxuryCard)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(HeroCardGradient)
                .padding(18.dp)
        ) {
            Text(
                text = "XP SUMMARY TELEMETRY",
                style = MaterialTheme.typography.labelSmall,
                color = IceCyanPrimary,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.2.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Daily XP
                MetricCard(
                    modifier = Modifier.weight(1f),
                    title = "DAILY XP",
                    value = "+$dailyXp",
                    icon = Icons.Default.Today,
                    color = IceCyanPrimary
                )

                // Weekly XP
                MetricCard(
                    modifier = Modifier.weight(1f),
                    title = "WEEKLY XP",
                    value = "+$weeklyXp",
                    icon = Icons.Default.ViewWeek,
                    color = ElectricBlue
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Monthly XP
                MetricCard(
                    modifier = Modifier.weight(1f),
                    title = "MONTHLY XP",
                    value = "+$monthlyXp",
                    icon = Icons.Default.DateRange,
                    color = PurpleArc
                )

                // Total XP
                MetricCard(
                    modifier = Modifier.weight(1f),
                    title = "TOTAL XP",
                    value = "$totalXp",
                    icon = Icons.Default.Star,
                    color = SuccessGreen
                )
            }
        }
    }
}

@Composable
fun MetricCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    icon: ImageVector,
    color: Color
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF070B16))
            .border(1.dp, GlassBorder, RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall,
                    color = GlassWhiteMuted,
                    fontSize = 10.sp
                )
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(14.dp)
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = color,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}

@Composable
fun XpTransactionItemCard(tx: XpTransactionEntity) {
    val categoryColor = getCategoryColor(tx.category)
    val categoryIcon = getCategoryIcon(tx.category)
    val formattedTime = formatTimestamp(tx.timestamp)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, GlassBorder, RoundedCornerShape(14.dp)),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = LuxuryCard)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Category Icon Capsule
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(categoryColor.copy(alpha = 0.15f))
                        .border(1.dp, categoryColor.copy(alpha = 0.4f), RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = categoryIcon,
                        contentDescription = tx.category,
                        tint = categoryColor,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = tx.title,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = GlassWhite
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = tx.category.uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = categoryColor,
                            fontSize = 10.sp
                        )
                        Text(
                            text = " • $formattedTime",
                            style = MaterialTheme.typography.labelSmall,
                            color = GlassWhiteMuted,
                            fontSize = 10.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Glowing XP Badge
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(categoryColor.copy(alpha = 0.15f))
                    .border(1.dp, categoryColor, RoundedCornerShape(8.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "+${tx.xp} XP",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = categoryColor,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}

@Composable
fun QuickXpLoggingCard(onLog: (String, String, Int) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, IceCyanPrimary.copy(alpha = 0.5f), RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = FrostedNavyCard)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "LOG PROTOCOL XP",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = GlassWhite
            )
            Text(
                text = "Award verified task execution to ledger",
                style = MaterialTheme.typography.labelSmall,
                color = GlassWhiteMuted
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { onLog("Deep Work Study Session", "Study", 60) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = IceCyanPrimary)
                ) {
                    Text("+60 Study", color = Color(0xFF050816), fontWeight = FontWeight.Bold, fontSize = 11.sp)
                }

                Button(
                    onClick = { onLog("Workout Routine Completed", "Workout", 40) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = FireOrange)
                ) {
                    Text("+40 Workout", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { onLog("Night Reflection & Accountability", "Discipline", 25) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PurpleArc)
                ) {
                    Text("+25 Reflection", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                }

                Button(
                    onClick = { onLog("Flashcard Formula Revision", "Revision", 15) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen)
                ) {
                    Text("+15 Flashcards", color = Color(0xFF050816), fontWeight = FontWeight.Bold, fontSize = 11.sp)
                }
            }
        }
    }
}

@Composable
fun EmptyTransactionsCard(query: String, category: String, onQuickLog: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, GlassBorder, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = LuxuryCard)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.ReceiptLong,
                contentDescription = null,
                tint = IceCyanPrimary.copy(alpha = 0.5f),
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "No Transactions Found",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = GlassWhite
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = if (query.isNotBlank()) "No records matching \"$query\" in $category." else "Execute study blocks, workouts, or reflections to build your ledger.",
                style = MaterialTheme.typography.bodySmall,
                color = GlassWhiteMuted
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onQuickLog,
                colors = ButtonDefaults.buttonColors(containerColor = IceCyanPrimary)
            ) {
                Text("Log Protocol XP", color = Color(0xFF050816), fontWeight = FontWeight.Bold)
            }
        }
    }
}

fun getCategoryColor(category: String): Color {
    return when (category.lowercase()) {
        "study" -> IceCyanPrimary
        "workout" -> FireOrange
        "discipline" -> PurpleArc
        "habit" -> SuccessGreen
        "school" -> ElectricBlue
        "revision" -> WarningAmber
        else -> LuxuryAccent
    }
}

fun getCategoryIcon(category: String): ImageVector {
    return when (category.lowercase()) {
        "study" -> Icons.Default.MenuBook
        "workout" -> Icons.Default.FitnessCenter
        "discipline" -> Icons.Default.Psychology
        "habit" -> Icons.Default.SelfImprovement
        "school" -> Icons.Default.School
        "revision" -> Icons.Default.Style
        else -> Icons.Default.Bolt
    }
}

fun formatTimestamp(timestamp: Long): String {
    val date = Date(timestamp)
    val now = Date()
    val timeFormat = SimpleDateFormat("hh:mm a", Locale.US)
    val dateFormat = SimpleDateFormat("MMM d, hh:mm a", Locale.US)

    val isSameDay = SimpleDateFormat("yyyyMMdd", Locale.US).format(date) ==
                    SimpleDateFormat("yyyyMMdd", Locale.US).format(now)

    return if (isSameDay) {
        timeFormat.format(date)
    } else {
        dateFormat.format(date)
    }
}
