package com.example.ui.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.R
import com.example.ui.components.GlowPill
import com.example.ui.screens.AiCoachScreen
import com.example.ui.screens.AnalyticsScreen
import com.example.ui.screens.BoardExamScreen
import com.example.ui.screens.FitnessScreen
import com.example.ui.screens.GoalsScreen
import com.example.ui.screens.HabitsScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.NotesScreen
import com.example.ui.screens.PlannerScreen
import com.example.ui.screens.PomodoroScreen
import com.example.ui.screens.ScheduleScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.SubjectsScreen
import com.example.ui.screens.TasksScreen
import com.example.ui.screens.WinterArcScreen
import com.example.ui.theme.DarkNavy
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.FireOrange
import com.example.ui.theme.FrostBlueAccent
import com.example.ui.theme.FrostedNavyCard
import com.example.ui.theme.GlassWhite
import com.example.ui.theme.GlassWhiteMuted
import com.example.ui.theme.IceCyanGlow
import com.example.ui.theme.IceCyanPrimary
import com.example.ui.theme.LuxuryAccent
import com.example.ui.theme.LuxuryCard
import com.example.ui.theme.PurpleArc
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.WarningAmber
import com.example.viewmodel.AiCoachViewModel
import com.example.viewmodel.AnalyticsViewModel
import com.example.viewmodel.BoardExamViewModel
import com.example.viewmodel.FitnessViewModel
import com.example.viewmodel.GoalsViewModel
import com.example.viewmodel.HabitsViewModel
import com.example.viewmodel.HomeViewModel
import com.example.viewmodel.NotesViewModel
import com.example.viewmodel.PlannerViewModel
import com.example.viewmodel.PomodoroViewModel
import com.example.viewmodel.SettingsViewModel
import com.example.viewmodel.SubjectsViewModel
import com.example.viewmodel.WinterArcViewModel
import kotlinx.coroutines.launch

sealed class Screen(val route: String, val title: String, val icon: ImageVector, val badgeText: String? = null) {
    object Home : Screen("home", "Dashboard", Icons.Default.Home)
    object AiCoach : Screen("ai_coach", "AI Coach", Icons.Default.Psychology, "Gemini")
    object Goals : Screen("goals", "Apex Goals", Icons.Default.EmojiEvents, "Targets")
    object Schedule : Screen("schedule", "Schedule", Icons.Default.CalendarMonth, "09:45-01:00")
    object WinterArc : Screen("winter_arc", "Winter Arc", Icons.Default.TrendingUp, "90D")
    object Subjects : Screen("subjects", "Study Tracker", Icons.Default.MenuBook, "70 Ch")
    object Tasks : Screen("tasks", "Tasks", Icons.Default.TaskAlt)
    object Focus : Screen("focus", "Focus & Pomodoro", Icons.Default.Timer)
    object Fitness : Screen("fitness", "Fitness & Calisthenics", Icons.Default.FitnessCenter)
    object Habits : Screen("habits", "Habits & Discipline", Icons.Default.CheckCircle)
    object BoardExam : Screen("board_exam", "Board Exam", Icons.Default.School, "148d")
    object Notes : Screen("notes", "Notes & Reflection", Icons.Default.Notes)
    object Analytics : Screen("analytics", "Analytics", Icons.Default.Analytics)
    object Settings : Screen("settings", "Settings", Icons.Default.Settings)
}

@Composable
fun RebuildAppScaffold(
    homeViewModel: HomeViewModel,
    plannerViewModel: PlannerViewModel,
    subjectsViewModel: SubjectsViewModel,
    pomodoroViewModel: PomodoroViewModel,
    fitnessViewModel: FitnessViewModel,
    goalsViewModel: GoalsViewModel,
    habitsViewModel: HabitsViewModel,
    winterArcViewModel: WinterArcViewModel,
    boardExamViewModel: BoardExamViewModel,
    analyticsViewModel: AnalyticsViewModel,
    settingsViewModel: SettingsViewModel,
    aiCoachViewModel: AiCoachViewModel,
    notesViewModel: NotesViewModel
) {
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: Screen.Home.route

    val homeUiState by homeViewModel.uiState.collectAsStateWithLifecycle()

    val openDrawer: () -> Unit = {
        coroutineScope.launch {
            drawerState.open()
        }
    }

    val closeDrawerAndNavigate: (String) -> Unit = { route ->
        coroutineScope.launch {
            drawerState.close()
            if (currentRoute != route) {
                navController.navigate(route) {
                    popUpTo(Screen.Home.route) { saveState = true }
                    launchSingleTop = true
                    restoreState = true
                }
            }
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = false, // CRITICAL: Drawer must NOT open via edge swipe gestures, only via ☰ Menu Button
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = Color(0xFF070D1E),
                drawerContentColor = GlassWhite,
                modifier = Modifier
                    .width(320.dp)
                    .fillMaxHeight()
                    .border(
                        BorderStroke(
                            1.dp,
                            Brush.verticalGradient(
                                listOf(
                                    IceCyanPrimary.copy(alpha = 0.5f),
                                    LuxuryAccent.copy(alpha = 0.3f),
                                    Color.Transparent
                                )
                            )
                        ),
                        RoundedCornerShape(topEnd = 24.dp, bottomEnd = 24.dp)
                    )
            ) {
                RebuildDrawerContent(
                    currentRoute = currentRoute,
                    xp = homeUiState.winterArcState.xp,
                    level = homeUiState.winterArcState.level,
                    arcDay = homeUiState.winterArcState.currentDay,
                    daysUntilExam = homeUiState.daysUntilExam,
                    onNavigate = closeDrawerAndNavigate
                )
            }
        }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            DarkNavy,
                            Color(0xFF070E22),
                            Color(0xFF040714)
                        )
                    )
                )
        ) {
            NavHost(
                navController = navController,
                startDestination = Screen.Home.route,
                modifier = Modifier.fillMaxSize(),
                enterTransition = { fadeIn(tween(180)) },
                exitTransition = { fadeOut(tween(180)) }
            ) {
                // 1. Dashboard / Home
                composable(Screen.Home.route) {
                    HomeScreen(
                        viewModel = homeViewModel,
                        onOpenDrawer = openDrawer,
                        onNavigateToSchool = { navController.navigate(Screen.Schedule.route) },
                        onNavigateToPlanner = { navController.navigate(Screen.Tasks.route) },
                        onNavigateToPomodoro = { navController.navigate(Screen.Focus.route) },
                        onNavigateToWinterArc = { navController.navigate(Screen.WinterArc.route) },
                        onNavigateToBoardExam = { navController.navigate(Screen.BoardExam.route) }
                    )
                }

                // 2. AI Coach (Gemini integration & blueprints)
                composable(Screen.AiCoach.route) {
                    AiCoachScreen(
                        viewModel = aiCoachViewModel,
                        onOpenDrawer = openDrawer
                    )
                }

                // 2.5 Goals (Target Milestones & Apex Objectives)
                composable(Screen.Goals.route) {
                    GoalsScreen(
                        viewModel = goalsViewModel,
                        onOpenDrawer = openDrawer
                    )
                }

                // 3. Schedule (09:45 AM - 01:00 PM School Flow & Time Blocking)
                composable(Screen.Schedule.route) {
                    ScheduleScreen(
                        viewModel = plannerViewModel,
                        onOpenDrawer = openDrawer
                    )
                }

                // 4. Winter Arc 90-Day Challenge
                composable(Screen.WinterArc.route) {
                    WinterArcScreen(
                        viewModel = winterArcViewModel,
                        onOpenDrawer = openDrawer
                    )
                }

                // 5. Study Tracker (70 Chapters Syllabus Mastery)
                composable(Screen.Subjects.route) {
                    SubjectsScreen(
                        viewModel = subjectsViewModel,
                        onOpenDrawer = openDrawer,
                        onStartFocusSession = { sub, chap ->
                            pomodoroViewModel.setSelectedSubjectAndChapter(sub, chap)
                            navController.navigate(Screen.Focus.route)
                        }
                    )
                }

                // 6. Tasks (Daily Protocols & Filtering)
                composable(Screen.Tasks.route) {
                    TasksScreen(
                        viewModel = plannerViewModel,
                        onOpenDrawer = openDrawer
                    )
                }

                // 7. Focus & Pomodoro
                composable(Screen.Focus.route) {
                    PomodoroScreen(
                        viewModel = pomodoroViewModel,
                        onOpenDrawer = openDrawer
                    )
                }

                // 8. Fitness & Calisthenics
                composable(Screen.Fitness.route) {
                    FitnessScreen(
                        viewModel = fitnessViewModel,
                        onOpenDrawer = openDrawer
                    )
                }

                // 9. Habits & Abstinence Matrix
                composable(Screen.Habits.route) {
                    HabitsScreen(
                        viewModel = habitsViewModel,
                        onOpenDrawer = openDrawer
                    )
                }

                // 10. Board Exam 2027 Mode
                composable(Screen.BoardExam.route) {
                    BoardExamScreen(
                        viewModel = boardExamViewModel,
                        onOpenDrawer = openDrawer
                    )
                }

                // 11. Notes & Reflection Journal
                composable(Screen.Notes.route) {
                    NotesScreen(
                        viewModel = notesViewModel,
                        onOpenDrawer = openDrawer
                    )
                }

                // 12. Analytics & Telemetry
                composable(Screen.Analytics.route) {
                    AnalyticsScreen(
                        viewModel = analyticsViewModel,
                        onOpenDrawer = openDrawer
                    )
                }

                // 13. Settings
                composable(Screen.Settings.route) {
                    SettingsScreen(
                        viewModel = settingsViewModel,
                        onOpenDrawer = openDrawer
                    )
                }
            }
        }
    }
}

@Composable
fun RebuildDrawerContent(
    currentRoute: String,
    xp: Int,
    level: Int,
    arcDay: Int,
    daysUntilExam: Long,
    onNavigate: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp),
        contentPadding = PaddingValues(top = 20.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // App Identity Header
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(bottom = 8.dp)
            ) {
                // Compact Brand Header
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Surface(
                        modifier = Modifier.size(36.dp),
                        shape = RoundedCornerShape(8.dp),
                        color = FrostedNavyCard,
                        border = BorderStroke(1.dp, IceCyanPrimary.copy(alpha = 0.5f))
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            androidx.compose.foundation.Image(
                                painter = painterResource(id = R.drawable.rebuild_logo),
                                contentDescription = "REBUILD Logo",
                                modifier = Modifier.size(26.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "REBUILD",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            color = GlassWhite,
                            fontSize = 17.sp
                        )
                        Text(
                            text = "Class 12 & Winter Arc OS",
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 11.sp,
                            color = GlassWhiteMuted
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Compact Telemetry Info Cards (2x2 Grid)
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // Current Level
                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            color = LuxuryCard,
                            border = BorderStroke(0.5.dp, Color(0x337C8CFF))
                        ) {
                            Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)) {
                                Text(
                                    text = "LEVEL $level",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = PurpleArc,
                                    fontSize = 12.sp
                                )
                                Text(
                                    text = "Rank Tier",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontSize = 10.sp,
                                    color = GlassWhiteMuted
                                )
                            }
                        }

                        // XP
                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            color = LuxuryCard,
                            border = BorderStroke(0.5.dp, Color(0x337C8CFF))
                        ) {
                            Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)) {
                                Text(
                                    text = "${String.format("%,d", xp)} XP",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = IceCyanPrimary,
                                    fontSize = 12.sp
                                )
                                Text(
                                    text = "Accumulated",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontSize = 10.sp,
                                    color = GlassWhiteMuted
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // Winter Arc Day
                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            color = LuxuryCard,
                            border = BorderStroke(0.5.dp, Color(0x337C8CFF))
                        ) {
                            Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)) {
                                Text(
                                    text = "Day $arcDay / 90",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = FrostBlueAccent,
                                    fontSize = 12.sp
                                )
                                Text(
                                    text = "Winter Arc",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontSize = 10.sp,
                                    color = GlassWhiteMuted
                                )
                            }
                        }

                        // Exam Countdown
                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            color = LuxuryCard,
                            border = BorderStroke(0.5.dp, Color(0x337C8CFF))
                        ) {
                            Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)) {
                                Text(
                                    text = "$daysUntilExam Days",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = WarningAmber,
                                    fontSize = 12.sp
                                )
                                Text(
                                    text = "To Board Exam",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontSize = 10.sp,
                                    color = GlassWhiteMuted
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider(color = Color(0x18FFFFFF), thickness = 0.5.dp)
            }
        }

        // Section 1: CORE PROTOCOL
        item {
            DrawerSectionHeader(title = "CORE PROTOCOL")
        }

        item {
            DrawerNavigationItem(
                screen = Screen.Home,
                isSelected = currentRoute == Screen.Home.route,
                onClick = { onNavigate(Screen.Home.route) }
            )
        }

        item {
            DrawerNavigationItem(
                screen = Screen.AiCoach,
                isSelected = currentRoute == Screen.AiCoach.route,
                highlightColor = LuxuryAccent,
                onClick = { onNavigate(Screen.AiCoach.route) }
            )
        }

        item {
            DrawerNavigationItem(
                screen = Screen.Goals,
                isSelected = currentRoute == Screen.Goals.route,
                highlightColor = LuxuryAccent,
                onClick = { onNavigate(Screen.Goals.route) }
            )
        }

        item {
            DrawerNavigationItem(
                screen = Screen.Schedule,
                isSelected = currentRoute == Screen.Schedule.route,
                onClick = { onNavigate(Screen.Schedule.route) }
            )
        }

        item {
            DrawerNavigationItem(
                screen = Screen.Tasks,
                isSelected = currentRoute == Screen.Tasks.route,
                onClick = { onNavigate(Screen.Tasks.route) }
            )
        }

        item {
            DrawerNavigationItem(
                screen = Screen.Focus,
                isSelected = currentRoute == Screen.Focus.route,
                onClick = { onNavigate(Screen.Focus.route) }
            )
        }

        // Section 2: ACADEMICS & TRANSFORMATION
        item {
            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = Color(0x11FFFFFF), thickness = 0.5.dp)
            Spacer(modifier = Modifier.height(4.dp))
            DrawerSectionHeader(title = "TRANSFORMATION & ACADEMICS")
        }

        item {
            DrawerNavigationItem(
                screen = Screen.WinterArc,
                isSelected = currentRoute == Screen.WinterArc.route,
                highlightColor = PurpleArc,
                onClick = { onNavigate(Screen.WinterArc.route) }
            )
        }

        item {
            DrawerNavigationItem(
                screen = Screen.Subjects,
                isSelected = currentRoute == Screen.Subjects.route,
                onClick = { onNavigate(Screen.Subjects.route) }
            )
        }

        item {
            DrawerNavigationItem(
                screen = Screen.BoardExam,
                isSelected = currentRoute == Screen.BoardExam.route,
                highlightColor = WarningAmber,
                onClick = { onNavigate(Screen.BoardExam.route) }
            )
        }

        item {
            DrawerNavigationItem(
                screen = Screen.Fitness,
                isSelected = currentRoute == Screen.Fitness.route,
                highlightColor = FireOrange,
                onClick = { onNavigate(Screen.Fitness.route) }
            )
        }

        item {
            DrawerNavigationItem(
                screen = Screen.Habits,
                isSelected = currentRoute == Screen.Habits.route,
                highlightColor = SuccessGreen,
                onClick = { onNavigate(Screen.Habits.route) }
            )
        }

        // Section 3: REFLECTIONS & TELEMETRY
        item {
            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = Color(0x11FFFFFF), thickness = 0.5.dp)
            Spacer(modifier = Modifier.height(4.dp))
            DrawerSectionHeader(title = "INSIGHTS & SYSTEM")
        }

        item {
            DrawerNavigationItem(
                screen = Screen.Notes,
                isSelected = currentRoute == Screen.Notes.route,
                onClick = { onNavigate(Screen.Notes.route) }
            )
        }

        item {
            DrawerNavigationItem(
                screen = Screen.Analytics,
                isSelected = currentRoute == Screen.Analytics.route,
                onClick = { onNavigate(Screen.Analytics.route) }
            )
        }

        item {
            DrawerNavigationItem(
                screen = Screen.Settings,
                isSelected = currentRoute == Screen.Settings.route,
                onClick = { onNavigate(Screen.Settings.route) }
            )
        }
    }
}

@Composable
private fun DrawerSectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelSmall,
        fontSize = 9.sp,
        fontWeight = FontWeight.ExtraBold,
        letterSpacing = 1.4.sp,
        color = IceCyanPrimary.copy(alpha = 0.8f),
        modifier = Modifier.padding(start = 12.dp, top = 6.dp, bottom = 4.dp)
    )
}

@Composable
private fun DrawerNavigationItem(
    screen: Screen,
    isSelected: Boolean,
    highlightColor: Color = IceCyanPrimary,
    onClick: () -> Unit
) {
    val activeColor = if (isSelected) highlightColor else GlassWhiteMuted
    val containerBg = if (isSelected) {
        highlightColor.copy(alpha = 0.15f)
    } else {
        Color.Transparent
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .testTag("drawer_item_${screen.route}"),
        shape = RoundedCornerShape(12.dp),
        color = containerBg,
        border = if (isSelected) BorderStroke(1.dp, highlightColor.copy(alpha = 0.5f)) else null
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = screen.icon,
                contentDescription = screen.title,
                tint = activeColor,
                modifier = Modifier.size(20.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = screen.title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) GlassWhite else GlassWhiteMuted,
                modifier = Modifier.weight(1f)
            )

            screen.badgeText?.let { badge ->
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = if (isSelected) highlightColor.copy(alpha = 0.25f) else Color(0x331E3A68)
                ) {
                    Text(
                        text = badge,
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) highlightColor else FrostBlueAccent,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
        }
    }
}
