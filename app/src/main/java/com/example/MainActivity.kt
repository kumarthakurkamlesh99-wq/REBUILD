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
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.navigation.RebuildAppScaffold
import com.example.ui.navigation.Screen
import com.example.ui.theme.DarkNavy
import com.example.ui.theme.REBUILDTheme
import com.example.viewmodel.AppInitViewModel
import com.example.viewmodel.AppInitViewModelFactory
import com.example.viewmodel.HomeViewModel
import com.example.viewmodel.HomeViewModelFactory
import com.example.viewmodel.OnboardingViewModel
import com.example.viewmodel.OnboardingViewModelFactory
import com.example.viewmodel.SettingsViewModel
import com.example.viewmodel.SettingsViewModelFactory

class MainActivity : ComponentActivity() {

    private val applicationInstance by lazy { application as RebuildApplication }

    private val appInitViewModel: AppInitViewModel by viewModels {
        AppInitViewModelFactory(
            applicationInstance.repository,
            applicationInstance.userPreferencesRepository
        )
    }

    // Critical ViewModels
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
        // Install Android 12+ system splash screen for seamless cold-start handoff
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val settingsUiState by settingsViewModel.uiState.collectAsStateWithLifecycle()
            val startupState by appInitViewModel.startupState.collectAsStateWithLifecycle()

            REBUILDTheme(darkTheme = settingsUiState.isDarkTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = DarkNavy
                ) {
                    RebuildAppScaffold(
                        application = applicationInstance,
                        homeViewModel = homeViewModel,
                        settingsViewModel = settingsViewModel,
                        onboardingViewModel = onboardingViewModel,
                        startDestination = Screen.Splash.route,
                        startupState = startupState,
                        onOnboardingComplete = {
                            appInitViewModel.onOnboardingFinished()
                        }
                    )
                }
            }
        }
    }
}
