package com.example.ui.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.navigationBarsPadding
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
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.R
import com.example.RebuildApplication
import com.example.ui.screens.AiChatScreen
import com.example.ui.screens.AiCoachScreen
import com.example.ui.screens.AlarmsScreen
import com.example.ui.screens.AnalyticsScreen
import com.example.ui.screens.BoardExamScreen
import com.example.ui.screens.FitnessScreen
import com.example.ui.screens.GoalsScreen
import com.example.ui.screens.HabitsScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.NotesScreen
import com.example.ui.screens.NotificationCenterScreen
import com.example.ui.screens.OnboardingScreen
import com.example.ui.screens.PlannerScreen
import com.example.ui.screens.PomodoroScreen
import com.example.ui.screens.ScheduleScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.SubjectsScreen
import com.example.ui.screens.SyllabusScreen
import com.example.ui.screens.TasksScreen
import com.example.ui.screens.WinterArcScreen
import com.example.ui.splash.RebuildSplashScreen
import com.example.ui.theme.DarkNavy
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.FireOrange
import com.example.ui.theme.FrostBlueAccent
import com.example.ui.theme.FrostedNavyCard
import com.example.ui.theme.GlassWhite
import com.example.ui.theme.GlassWhiteMuted
import com.example.ui.theme.IceCyanPrimary
import com.example.ui.theme.LuxuryAccent
import com.example.ui.theme.LuxuryCard
import com.example.ui.theme.PurpleArc
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.WarningAmber
import com.example.viewmodel.AiChatViewModel
import com.example.viewmodel.AiChatViewModelFactory
import com.example.viewmodel.AiCoachViewModel
import com.example.viewmodel.AiCoachViewModelFactory
import com.example.viewmodel.AlarmsViewModel
import com.example.viewmodel.AlarmsViewModelFactory
import com.example.viewmodel.AnalyticsViewModel
import com.example.viewmodel.AnalyticsViewModelFactory
import com.example.viewmodel.BoardExamViewModel
import com.example.viewmodel.BoardExamViewModelFactory
import com.example.viewmodel.FitnessViewModel
import com.example.viewmodel.FitnessViewModelFactory
import com.example.viewmodel.GoalsViewModel
import com.example.viewmodel.GoalsViewModelFactory
import com.example.viewmodel.HabitsViewModel
import com.example.viewmodel.HabitsViewModelFactory
import com.example.viewmodel.HomeViewModel
import com.example.viewmodel.NotesViewModel
import com.example.viewmodel.NotesViewModelFactory
import com.example.viewmodel.OnboardingViewModel
import com.example.viewmodel.PlannerViewModel
import com.example.viewmodel.PlannerViewModelFactory
import com.example.viewmodel.PomodoroViewModel
import com.example.viewmodel.PomodoroViewModelFactory
import com.example.viewmodel.SettingsViewModel
import com.example.viewmodel.SubjectsViewModel
import com.example.viewmodel.SubjectsViewModelFactory
import com.example.viewmodel.SyllabusViewModel
import com.example.viewmodel.SyllabusViewModelFactory
import com.example.viewmodel.WinterArcViewModel
import com.example.viewmodel.WinterArcViewModelFactory
import kotlinx.coroutines.launch

sealed class Screen(val route: String, val title: String, val icon: ImageVector, val badgeText: String? = null) {
    object Splash : Screen("splash", "Splash", Icons.Default.Bolt)
    object Onboarding : Screen("onboarding", "Profile Calibration", Icons.Default.Tune)
    object Home : Screen("home", "Dashboard", Icons.Default.Home)
    object AiChat : Screen("ai_chat", "AI Neural Chat", Icons.Default.Psychology, "Live AI")
    object AiCoach : Screen("ai_coach", "AI Plans Generator", Icons.Default.AutoAwesome, "Gemini")
    object Goals : Screen("goals", "Apex Goals", Icons.Default.EmojiEvents, "Targets")
    object Schedule : Screen("schedule", "Schedule", Icons.Default.CalendarMonth)
    object WinterArc : Screen("winter_arc", "Winter Arc Mission Control", Icons.Default.TrendingUp, "90D Arc")
    object Syllabus : Screen("syllabus", "Class 12 Syllabus", Icons.Default.MenuBook, "70 Chaps")
    object Subjects : Screen("subjects", "Study Tracker", Icons.Default.School)
    object Tasks : Screen("tasks", "Tasks", Icons.Default.TaskAlt)
    object Focus : Screen("focus", "Focus & Pomodoro", Icons.Default.Timer)
    object Fitness : Screen("fitness", "Fitness & Calisthenics", Icons.Default.FitnessCenter)
    object Habits : Screen("habits", "Habits & Discipline", Icons.Default.CheckCircle)
    object BoardExam : Screen("board_exam", "Board Exam Blueprint", Icons.Default.School)
    object Alarms : Screen("alarms", "Smart Alarm Engine", Icons.Default.NotificationsActive, "Challenges")
    object Notes : Screen("notes", "Notes & Reflection", Icons.Default.Notes)
    object Analytics : Screen("analytics", "Analytics", Icons.Default.Analytics)
    object Notifications : Screen("notifications", "Notification Hub", Icons.Default.NotificationsActive, "9 Alarms")
    object Settings : Screen("settings", "Settings", Icons.Default.Settings)
    object RankReport : Screen("rank_report", "Rank Intelligence Report", Icons.Default.EmojiEvents)
    object XpLedger : Screen("xp_ledger", "XP Ledger", Icons.Default.Bolt)
    object ProfileSettings : Screen("profile_settings", "Profile Settings", Icons.Default.Person)
    object Certificate : Screen("certificate", "Certificate Engine", Icons.Default.WorkspacePremium, "Official")
}

@Composable
fun RebuildAppScaffold(
    application: RebuildApplication,
    homeViewModel: HomeViewModel,
    settingsViewModel: SettingsViewModel,
    onboardingViewModel: OnboardingViewModel,
    startDestination: String = Screen.Splash.route,
    startupState: com.example.viewmodel.AppStartupState = com.example.viewmodel.AppStartupState.Loading,
    onOnboardingComplete: () -> Unit = {}
) {
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: startDestination

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

    val userProfile = homeUiState.userProfile

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = false,
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
                    userName = userProfile?.name ?: "Kamlesh Kumar Thakur",
                    userClass = "${userProfile?.studentClass ?: "Class 12"} • ${userProfile?.stream ?: "Science (PCM)"}",
                    avatarUri = userProfile?.avatarUri ?: "",
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
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            NavHost(
                navController = navController,
                startDestination = startDestination,
                modifier = Modifier.fillMaxSize(),
                enterTransition = { fadeIn(tween(180)) },
                exitTransition = { fadeOut(tween(180)) }
            ) {
                // -1. Splash Screen
                composable(Screen.Splash.route) {
                    RebuildSplashScreen(
                        onFinished = {
                            val targetRoute = if (startupState is com.example.viewmodel.AppStartupState.NeedsOnboarding) {
                                Screen.Onboarding.route
                            } else {
                                Screen.Home.route
                            }
                            navController.navigate(targetRoute) {
                                popUpTo(Screen.Splash.route) { inclusive = true }
                            }
                        }
                    )
                }

                // 0. Onboarding
                composable(Screen.Onboarding.route) {
                    OnboardingScreen(
                        viewModel = onboardingViewModel,
                        onComplete = {
                            onOnboardingComplete()
                            navController.navigate(Screen.Home.route) {
                                popUpTo(Screen.Onboarding.route) { inclusive = true }
                            }
                        }
                    )
                }

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

                // 1.5 AI Neural Chat (Lazy Loaded)
                composable(Screen.AiChat.route) {
                    val aiChatVm: AiChatViewModel = viewModel(
                        factory = AiChatViewModelFactory(
                            application.geminiCoachRepository,
                            application.repository
                        )
                    )
                    AiChatScreen(
                        viewModel = aiChatVm,
                        onOpenDrawer = openDrawer
                    )
                }

                // 2. AI Coach Plans (Lazy Loaded)
                composable(Screen.AiCoach.route) {
                    val aiCoachVm: AiCoachViewModel = viewModel(
                        factory = AiCoachViewModelFactory(
                            application.geminiCoachRepository,
                            application.repository,
                            application.userPreferencesRepository
                        )
                    )
                    AiCoachScreen(
                        viewModel = aiCoachVm,
                        onOpenDrawer = openDrawer
                    )
                }

                // 2.5 Goals (Lazy Loaded)
                composable(Screen.Goals.route) {
                    val goalsVm: GoalsViewModel = viewModel(
                        factory = GoalsViewModelFactory(application.repository)
                    )
                    GoalsScreen(
                        viewModel = goalsVm,
                        onOpenDrawer = openDrawer
                    )
                }

                // 3. Schedule (Lazy Loaded)
                composable(Screen.Schedule.route) {
                    val plannerVm: PlannerViewModel = viewModel(
                        factory = PlannerViewModelFactory(application.repository)
                    )
                    ScheduleScreen(
                        viewModel = plannerVm,
                        onOpenDrawer = openDrawer
                    )
                }

                // 4. Winter Arc Mission Control (Lazy Loaded)
                composable(Screen.WinterArc.route) {
                    val winterArcVm: WinterArcViewModel = viewModel(
                        factory = WinterArcViewModelFactory(application.repository)
                    )
                    WinterArcScreen(
                        viewModel = winterArcVm,
                        onOpenDrawer = openDrawer
                    )
                }

                // 4.5 Class 12 Syllabus Tracker (Lazy Loaded)
                composable(Screen.Syllabus.route) {
                    val syllabusVm: SyllabusViewModel = viewModel(
                        factory = SyllabusViewModelFactory(application.repository)
                    )
                    SyllabusScreen(
                        viewModel = syllabusVm,
                        onOpenDrawer = openDrawer
                    )
                }

                // 5. Study Tracker (Lazy Loaded)
                composable(Screen.Subjects.route) {
                    val subjectsVm: SubjectsViewModel = viewModel(
                        factory = SubjectsViewModelFactory(application.repository)
                    )
                    val pomodoroVm: PomodoroViewModel = viewModel(
                        factory = PomodoroViewModelFactory(application.repository)
                    )
                    SubjectsScreen(
                        viewModel = subjectsVm,
                        onOpenDrawer = openDrawer,
                        onStartFocusSession = { sub, chap ->
                            pomodoroVm.setSelectedSubjectAndChapter(sub, chap)
                            navController.navigate(Screen.Focus.route)
                        }
                    )
                }

                // 5.5 Smart Alarm Engine (Lazy Loaded)
                composable(Screen.Alarms.route) {
                    val alarmsVm: AlarmsViewModel = viewModel(
                        factory = AlarmsViewModelFactory(application.repository)
                    )
                    AlarmsScreen(
                        viewModel = alarmsVm,
                        onOpenDrawer = openDrawer
                    )
                }

                // 6. Tasks (Lazy Loaded)
                composable(Screen.Tasks.route) {
                    val plannerVm: PlannerViewModel = viewModel(
                        factory = PlannerViewModelFactory(application.repository)
                    )
                    TasksScreen(
                        viewModel = plannerVm,
                        onOpenDrawer = openDrawer
                    )
                }

                // 7. Focus & Pomodoro (Lazy Loaded)
                composable(Screen.Focus.route) {
                    val pomodoroVm: PomodoroViewModel = viewModel(
                        factory = PomodoroViewModelFactory(application.repository)
                    )
                    PomodoroScreen(
                        viewModel = pomodoroVm,
                        onOpenDrawer = openDrawer
                    )
                }

                // 8. Fitness & Calisthenics (Lazy Loaded)
                composable(Screen.Fitness.route) {
                    val fitnessVm: FitnessViewModel = viewModel(
                        factory = FitnessViewModelFactory(application.repository)
                    )
                    FitnessScreen(
                        viewModel = fitnessVm,
                        onOpenDrawer = openDrawer
                    )
                }

                // 9. Habits & Matrix (Lazy Loaded)
                composable(Screen.Habits.route) {
                    val habitsVm: HabitsViewModel = viewModel(
                        factory = HabitsViewModelFactory(application.repository)
                    )
                    HabitsScreen(
                        viewModel = habitsVm,
                        onOpenDrawer = openDrawer
                    )
                }

                // 10. Board Exam (Lazy Loaded)
                composable(Screen.BoardExam.route) {
                    val boardExamVm: BoardExamViewModel = viewModel(
                        factory = BoardExamViewModelFactory(application.repository)
                    )
                    BoardExamScreen(
                        viewModel = boardExamVm,
                        onOpenDrawer = openDrawer
                    )
                }

                // 11. Notes & Reflection (Lazy Loaded)
                composable(Screen.Notes.route) {
                    val notesVm: NotesViewModel = viewModel(
                        factory = NotesViewModelFactory(application.repository)
                    )
                    NotesScreen(
                        viewModel = notesVm,
                        onOpenDrawer = openDrawer
                    )
                }

                // 12. Analytics (Lazy Loaded)
                composable(Screen.Analytics.route) {
                    val analyticsVm: AnalyticsViewModel = viewModel(
                        factory = AnalyticsViewModelFactory(application.repository)
                    )
                    AnalyticsScreen(
                        viewModel = analyticsVm,
                        onOpenDrawer = openDrawer
                    )
                }

                // 13. Notification Center & Diagnostics
                composable(Screen.Notifications.route) {
                    NotificationCenterScreen(
                        userProfile = homeUiState.userProfile,
                        onBack = { navController.popBackStack() }
                    )
                }

                // 14. Settings
                composable(Screen.Settings.route) {
                    SettingsScreen(
                        viewModel = settingsViewModel,
                        userProfile = homeUiState.userProfile,
                        onOpenDrawer = openDrawer,
                        onNavigateToNotifications = { navController.navigate(Screen.Notifications.route) },
                        onNavigateToProfileSettings = { navController.navigate(Screen.ProfileSettings.route) }
                    )
                }

                // 15. Rank Intelligence Report (Interactive Level System)
                composable(Screen.RankReport.route) {
                    val rankVm: com.example.viewmodel.RankReportViewModel = viewModel(
                        factory = com.example.viewmodel.RankReportViewModelFactory(application.repository)
                    )
                    com.example.ui.screens.RankReportScreen(
                        viewModel = rankVm,
                        onNavigateBack = { navController.popBackStack() },
                        onNavigateToCertificate = { navController.navigate(Screen.Certificate.route) },
                        onNavigateToCertificateWithLevel = { lvl ->
                            navController.navigate("${Screen.Certificate.route}?level=$lvl")
                        }
                    )
                }

                // 15B. REBUILD Certificate Engine (Official A4 Achievement Authority)
                composable(
                    route = "${Screen.Certificate.route}?level={level}",
                    arguments = listOf(
                        androidx.navigation.navArgument("level") {
                            type = androidx.navigation.NavType.IntType
                            defaultValue = -1
                        }
                    )
                ) { backStackEntry ->
                    val initialLevelArg = backStackEntry.arguments?.getInt("level")
                    val selectedLevel = if (initialLevelArg != null && initialLevelArg in 1..25) initialLevelArg else null
                    val certVm: com.example.viewmodel.CertificateViewModel = viewModel(
                        factory = com.example.viewmodel.CertificateViewModelFactory(application.repository)
                    )
                    com.example.ui.screens.CertificateScreen(
                        viewModel = certVm,
                        initialLevel = selectedLevel,
                        onNavigateBack = { navController.popBackStack() }
                    )
                }

                // 16. XP Ledger (Interactive XP System)
                composable(Screen.XpLedger.route) {
                    val xpVm: com.example.viewmodel.XpLedgerViewModel = viewModel(
                        factory = com.example.viewmodel.XpLedgerViewModelFactory(application.repository)
                    )
                    com.example.ui.screens.XpLedgerScreen(
                        viewModel = xpVm,
                        onNavigateBack = { navController.popBackStack() }
                    )
                }

                // 17. Profile Settings (Interactive Profile System)
                composable(Screen.ProfileSettings.route) {
                    val profileVm: com.example.viewmodel.ProfileSettingsViewModel = viewModel(
                        factory = com.example.viewmodel.ProfileSettingsViewModelFactory(application.repository)
                    )
                    com.example.ui.screens.ProfileSettingsScreen(
                        viewModel = profileVm,
                        onNavigateBack = { navController.popBackStack() }
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
    userName: String,
    userClass: String,
    avatarUri: String = "",
    onNavigate: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp),
        contentPadding = PaddingValues(top = 20.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // App Identity Header & Profile (Clickable to open Profile Settings)
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(bottom = 8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onNavigate(Screen.ProfileSettings.route) }
                        .padding(vertical = 4.dp, horizontal = 2.dp)
                ) {
                    Surface(
                        modifier = Modifier.size(42.dp),
                        shape = CircleShape,
                        color = FrostedNavyCard,
                        border = BorderStroke(1.5.dp, IceCyanPrimary)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            if (avatarUri.isNotBlank()) {
                                coil.compose.AsyncImage(
                                    model = avatarUri,
                                    contentDescription = "Avatar",
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(CircleShape)
                                )
                            } else {
                                Image(
                                    painter = painterResource(id = R.drawable.rebuild_logo),
                                    contentDescription = "REBUILD Logo",
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = userName.ifBlank { "Kamlesh Kumar Thakur" },
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp,
                            color = GlassWhite,
                            fontSize = 15.sp,
                            maxLines = 1
                        )
                        Text(
                            text = userClass,
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 11.sp,
                            color = IceCyanPrimary
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFF131D38),
                        border = BorderStroke(1.dp, IceCyanPrimary.copy(alpha = 0.3f))
                    ) {
                        Text(
                            text = "EDIT",
                            style = MaterialTheme.typography.labelSmall,
                            color = IceCyanPrimary,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
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
                        // Level Card (Clickable to open Rank Intelligence Report)
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { onNavigate(Screen.RankReport.route) },
                            shape = RoundedCornerShape(10.dp),
                            color = LuxuryCard,
                            border = BorderStroke(1.dp, PurpleArc.copy(alpha = 0.6f))
                        ) {
                            Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "LEVEL $level",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = PurpleArc,
                                        fontSize = 12.sp
                                    )
                                    Icon(
                                        imageVector = Icons.Default.Bolt,
                                        contentDescription = null,
                                        tint = PurpleArc,
                                        modifier = Modifier.size(12.dp)
                                    )
                                }
                                Text(
                                    text = "Rank Tier • Tap",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontSize = 10.sp,
                                    color = GlassWhiteMuted
                                )
                            }
                        }

                        // XP Card (Clickable to open XP Ledger)
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { onNavigate(Screen.XpLedger.route) },
                            shape = RoundedCornerShape(10.dp),
                            color = LuxuryCard,
                            border = BorderStroke(1.dp, IceCyanPrimary.copy(alpha = 0.6f))
                        ) {
                            Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "${String.format("%,d", xp)} XP",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = IceCyanPrimary,
                                        fontSize = 12.sp
                                    )
                                    Icon(
                                        imageVector = Icons.Default.Bolt,
                                        contentDescription = null,
                                        tint = IceCyanPrimary,
                                        modifier = Modifier.size(12.dp)
                                    )
                                }
                                Text(
                                    text = "Ledger • Tap",
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
                                    text = "To Target Exam",
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

        // Section 1: CORE PROTOCOL & AI
        item {
            DrawerSectionHeader(title = "CORE PROTOCOL & AI")
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
                screen = Screen.AiChat,
                isSelected = currentRoute == Screen.AiChat.route,
                highlightColor = IceCyanPrimary,
                onClick = { onNavigate(Screen.AiChat.route) }
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
                screen = Screen.Certificate,
                isSelected = currentRoute == Screen.Certificate.route,
                highlightColor = LuxuryAccent,
                onClick = { onNavigate(Screen.Certificate.route) }
            )
        }

        item {
            DrawerNavigationItem(
                screen = Screen.Syllabus,
                isSelected = currentRoute == Screen.Syllabus.route,
                highlightColor = IceCyanPrimary,
                onClick = { onNavigate(Screen.Syllabus.route) }
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
                screen = Screen.Alarms,
                isSelected = currentRoute == Screen.Alarms.route,
                highlightColor = IceCyanPrimary,
                onClick = { onNavigate(Screen.Alarms.route) }
            )
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
                screen = Screen.Notifications,
                isSelected = currentRoute == Screen.Notifications.route,
                highlightColor = SuccessGreen,
                onClick = { onNavigate(Screen.Notifications.route) }
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
