package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = IceCyanPrimary,
    onPrimary = DarkNavy,
    primaryContainer = Color(0xFF132D54),
    onPrimaryContainer = IceCyanPrimary,
    secondary = FrostBlueAccent,
    onSecondary = DarkNavy,
    secondaryContainer = Color(0xFF1B2E4B),
    onSecondaryContainer = FrostBlueAccent,
    tertiary = PurpleArc,
    onTertiary = DarkNavy,
    background = DarkNavy,
    onBackground = GlassWhite,
    surface = DeepNavySurface,
    onSurface = GlassWhite,
    surfaceVariant = FrostedNavyCard,
    onSurfaceVariant = GlassWhiteMuted,
    outline = FrostedNavyCardBorder,
    error = DangerRed,
    onError = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF006C84),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFC7F0FA),
    onPrimaryContainer = Color(0xFF001F29),
    secondary = Color(0xFF4C626B),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFCFE6F1),
    onSecondaryContainer = Color(0xFF071E26),
    tertiary = Color(0xFF6B5778),
    onTertiary = Color.White,
    background = Color(0xFFF6F9FD),
    onBackground = Color(0xFF171C1F),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF171C1F),
    surfaceVariant = Color(0xFFDEE4E9),
    onSurfaceVariant = Color(0xFF41484D),
    outline = Color(0xFF71787D),
    error = DangerRed,
    onError = Color.White
)

@Composable
fun REBUILDTheme(
    darkTheme: Boolean = true, // Default to Liquid Glassmorphism Dark Mode for Winter Arc vibe
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = Color.Transparent.toArgb()
            window.navigationBarColor = Color.Transparent.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
