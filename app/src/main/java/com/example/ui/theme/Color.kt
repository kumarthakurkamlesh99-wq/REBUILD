package com.example.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Dark Luxury Palette
val DarkLuxuryBackground = Color(0xFF050816)
val DarkNavy = Color(0xFF050816)
val LuxuryCard = Color(0xFF0E1629)
val DeepNavySurface = Color(0xFF0E1629)
val MidnightNavy = Color(0xFF02040A)
val FrostedNavyCard = Color(0xEE0E1629)
val FrostedNavyCardBorder = Color(0x337C8CFF)

// Accent & Status Colors
val LuxuryAccent = Color(0xFF7C8CFF)
val IceCyanPrimary = Color(0xFF7C8CFF)
val IceCyanGlow = Color(0xFF94A3FF)
val FrostBlueAccent = Color(0xFF60A5FA)
val ElectricBlue = Color(0xFF3B82F6)

val GlassWhite = Color(0xFFFFFFFF)
val GlassWhiteMuted = Color(0xFF94A3B8)
val GlassBorder = Color(0x2E7C8CFF)
val GlassHighlight = Color(0x15FFFFFF)

val SuccessGreen = Color(0xFF4ADE80)
val WarningAmber = Color(0xFFFACC15)
val FireOrange = Color(0xFFFF6B4A)
val DangerRed = Color(0xFFF87171)
val PurpleArc = Color(0xFFA78BFA)

// Gradients
val LiquidArcGradient = Brush.linearGradient(
    colors = listOf(Color(0xFF7C8CFF), Color(0xFF60A5FA), Color(0xFF38BDF8))
)

val LuxuryHeroGradient = Brush.verticalGradient(
    colors = listOf(
        Color(0xFF131D38),
        Color(0xFF0E1629),
        Color(0xFF050816)
    )
)

val HeroCardGradient = LuxuryHeroGradient

val GlassSurfaceGradient = Brush.verticalGradient(
    colors = listOf(
        Color(0x257C8CFF),
        Color(0x0A0E1629)
    )
)

val GlowBorderBrush = Brush.linearGradient(
    colors = listOf(
        Color(0x667C8CFF),
        Color(0x2260A5FA),
        Color(0x447C8CFF)
    )
)

val FireStreakGradient = Brush.horizontalGradient(
    colors = listOf(Color(0xFFFB923C), Color(0xFFEF4444))
)

val DisciplineScoreGradient = Brush.linearGradient(
    colors = listOf(Color(0xFF7C8CFF), Color(0xFF38BDF8), Color(0xFF4ADE80))
)

