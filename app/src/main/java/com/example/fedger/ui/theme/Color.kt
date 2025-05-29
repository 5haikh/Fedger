package com.example.fedger.ui.theme

import androidx.compose.ui.graphics.Color

// Modern enhanced color palette
val DeepPurple = Color(0xFF000000) // True black background
val DarkPurple = Color(0xFF2A1B47) // Card backgrounds
val MediumPurple = Color(0xFF6200EE) // Primary actions
val LightPurple = Color(0xFFB39DFF) // Slightly lighter for visibility on black
val PurpleHighlight = Color(0xFFBB86FC) // Special highlights

// Text colors with improved contrast
val TextWhite = Color(0xFFFFFFFF) // Pure white for best contrast on black
val TextGrey = Color(0xFFB0B0B0) // Lighter grey for better contrast on black
val HighContrastGrey = Color(0xFFD0D0D0) // Slightly darker for less glare on black
val TextRed = Color(0xFFFF5252)
val TextGreen = Color(0xFF4CAF50)

// Surface and card colors
val CardBackground = Color(0xFF111111) // Near black for cards
val SurfaceLight = Color(0xFF222222) // Darker for black theme, but still distinct from pure black
val SurfaceDark = Color(0xFF181818) // Near black for surfaces

// Secondary colors
val AccentAmber = Color(0xFFFFB74D)
val AccentTeal = Color(0xFF03DAC5)

// Legacy Material colors - keeping for compatibility
val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)

val Purple40 = Color(0xFF6650a4)
val PurpleGrey40 = Color(0xFF625b71)
val Pink40 = Color(0xFF7D5260)

// Light Theme Colors
val LightBackground = Color(0xFFFFFFFF) // White
val LightSurface = Color(0xFFF0F0F0) // Light Gray for cards/surfaces
val TextBlack = Color(0xFF000000) // Black for text on light backgrounds
val TextGrayLight = Color(0xFF555555) // Darker gray for secondary text on light backgrounds
val PrimaryLight = Color(0xFF6200EE) // Can remain the same as MediumPurple or choose a lighter variant if needed
val OnPrimaryLight = Color(0xFFFFFFFF) // Text/icons on Primary color - likely white
val SecondaryLight = Color(0xFF03DAC5) // Can remain the same as AccentTeal or choose a different accent
val TertiaryLight = Color(0xFF03DAC5) // Can remain the same or choose a different accent
val ErrorLight = Color(0xFFB00020) // Standard error color for light themes
val OnErrorLight = Color(0xFFFFFFFF) // Text/icons on Error color
val SurfaceVariantLight = Color(0xFFE0E0E0)
val OnSurfaceVariantLight = Color(0xFF424242)