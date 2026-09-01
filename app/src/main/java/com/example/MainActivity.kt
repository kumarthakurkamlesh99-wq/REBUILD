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
import com.example.viewmodel.HomeViewModel
import com.example.viewmodel.HomeViewModelFactory
import com.example.viewmodel.OnboardingViewModel
import com.example.viewmodel.OnboardingViewModelFactory
import com.example.viewmodel.SettingsViewModel
import com.example.viewmodel.SettingsViewModelFactory

class MainActivity : ComponentActivity() {

    private val applicationInstance by lazy { application as RebuildApplication }

    // Critical ViewModels required for initial screen rendering
    private val homeViewModel: HomeViewModel by viewModels {
        HomeViewModelFactory(applicationInstance.repository)
    }

    private val settingsViewModel: SettingsViewModel by viewModels {
        SettingsViewModelFactory(
            applicationInstance.userPreferencesRepository,
            applicationContext
        )
    }

    private val onboardingViewModel: OnboardingViewModel by viewModels {
        OnboardingViewModelFactory(
            applicationInstance.repository,
            applicationInstance.userPreferencesRepository
        )
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
                        application = applicationInstance,
                        homeViewModel = homeViewModel,
                        settingsViewModel = settingsViewModel,
                        onboardingViewModel = onboardingViewModel
                    )
                }
            }
        }
    }
}
