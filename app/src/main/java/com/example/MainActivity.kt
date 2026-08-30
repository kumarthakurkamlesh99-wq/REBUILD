package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.navigation.RebuildAppScaffold
import com.example.ui.theme.DarkNavy
import com.example.ui.theme.REBUILDTheme
import com.example.viewmodel.AiCoachViewModel
import com.example.viewmodel.AiCoachViewModelFactory
import com.example.viewmodel.AnalyticsViewModel
import com.example.viewmodel.AnalyticsViewModelFactory
import com.example.viewmodel.BoardExamViewModel
import com.example.viewmodel.BoardExamViewModelFactory
import com.example.viewmodel.FitnessViewModel
import com.example.viewmodel.FitnessViewModelFactory
import com.example.viewmodel.HabitsViewModel
import com.example.viewmodel.HabitsViewModelFactory
import com.example.viewmodel.HomeViewModel
import com.example.viewmodel.HomeViewModelFactory
import com.example.viewmodel.NotesViewModel
import com.example.viewmodel.NotesViewModelFactory
import com.example.viewmodel.PlannerViewModel
import com.example.viewmodel.PlannerViewModelFactory
import com.example.viewmodel.PomodoroViewModel
import com.example.viewmodel.PomodoroViewModelFactory
import com.example.viewmodel.SettingsViewModel
import com.example.viewmodel.SettingsViewModelFactory
import com.example.viewmodel.SubjectsViewModel
import com.example.viewmodel.SubjectsViewModelFactory
import com.example.viewmodel.WinterArcViewModel
import com.example.viewmodel.WinterArcViewModelFactory

class MainActivity : ComponentActivity() {

    private val applicationInstance by lazy { application as RebuildApplication }

    private val homeViewModel: HomeViewModel by viewModels {
        HomeViewModelFactory(applicationInstance.repository)
    }

    private val plannerViewModel: PlannerViewModel by viewModels {
        PlannerViewModelFactory(applicationInstance.repository)
    }

    private val subjectsViewModel: SubjectsViewModel by viewModels {
        SubjectsViewModelFactory(applicationInstance.repository)
    }

    private val pomodoroViewModel: PomodoroViewModel by viewModels {
        PomodoroViewModelFactory(applicationInstance.repository)
    }

    private val fitnessViewModel: FitnessViewModel by viewModels {
        FitnessViewModelFactory(applicationInstance.repository)
    }

    private val habitsViewModel: HabitsViewModel by viewModels {
        HabitsViewModelFactory(applicationInstance.repository)
    }

    private val winterArcViewModel: WinterArcViewModel by viewModels {
        WinterArcViewModelFactory(applicationInstance.repository)
    }

    private val boardExamViewModel: BoardExamViewModel by viewModels {
        BoardExamViewModelFactory(applicationInstance.repository)
    }

    private val analyticsViewModel: AnalyticsViewModel by viewModels {
        AnalyticsViewModelFactory(applicationInstance.repository)
    }

    private val settingsViewModel: SettingsViewModel by viewModels {
        SettingsViewModelFactory(
            applicationInstance.userPreferencesRepository,
            applicationContext
        )
    }

    private val aiCoachViewModel: AiCoachViewModel by viewModels {
        AiCoachViewModelFactory(
            applicationInstance.geminiCoachRepository,
            applicationInstance.repository,
            applicationInstance.userPreferencesRepository
        )
    }

    private val notesViewModel: NotesViewModel by viewModels {
        NotesViewModelFactory(applicationInstance.repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val settingsUiState by settingsViewModel.uiState.collectAsStateWithLifecycle()

            REBUILDTheme(darkTheme = settingsUiState.isDarkTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = DarkNavy
                ) {
                    RebuildAppScaffold(
                        homeViewModel = homeViewModel,
                        plannerViewModel = plannerViewModel,
                        subjectsViewModel = subjectsViewModel,
                        pomodoroViewModel = pomodoroViewModel,
                        fitnessViewModel = fitnessViewModel,
                        habitsViewModel = habitsViewModel,
                        winterArcViewModel = winterArcViewModel,
                        boardExamViewModel = boardExamViewModel,
                        analyticsViewModel = analyticsViewModel,
                        settingsViewModel = settingsViewModel,
                        aiCoachViewModel = aiCoachViewModel,
                        notesViewModel = notesViewModel
                    )
                }
            }
        }
    }
}
