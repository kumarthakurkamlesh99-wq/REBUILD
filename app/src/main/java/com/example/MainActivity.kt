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
import androidx.activity.result.contract.ActivityResultContracts
import android.os.Build
import com.example.notification.AlarmScheduler
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

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        // Notification permission granted or denied
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        // Install Android 12+ system splash screen for seamless cold-start handoff
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (!AlarmScheduler.hasNotificationPermission(this)) {
                notificationPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
            }
        }

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
