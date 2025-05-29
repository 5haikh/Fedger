package com.example.fedger.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import com.example.fedger.datastore.ThemePreferenceManager
import com.example.fedger.datastore.ThemeSetting
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = MediumPurple,
    onPrimary = TextWhite,
    secondary = LightPurple,
    tertiary = AccentTeal,
    background = DeepPurple,
    surface = CardBackground,
    onSurface = TextWhite,
    onBackground = TextWhite,
    error = TextRed,
    onError = TextWhite,
    surfaceVariant = SurfaceLight,
    onSurfaceVariant = TextGrey
)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryLight,
    onPrimary = OnPrimaryLight,
    secondary = SecondaryLight,
    tertiary = TertiaryLight,
    background = LightBackground,
    surface = LightSurface,
    onSurface = TextBlack,
    onBackground = TextBlack,
    error = ErrorLight,
    onError = OnErrorLight,
    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = OnSurfaceVariantLight
)

// Enhanced shapes for a more modern look
val AppShapes = Shapes(
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(24.dp)
)

@Composable
fun FedgerTheme(
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false, // Disable dynamic colors to use our custom theme
    content: @Composable () -> Unit
) {
    val context = LocalContext.current.applicationContext
    val themePreferenceManager = ThemePreferenceManager(context)
    val currentThemeSetting by themePreferenceManager.themeSetting.collectAsState(initial = ThemeSetting.SYSTEM)

    val useDarkTheme = when (currentThemeSetting) {
        ThemeSetting.LIGHT -> false
        ThemeSetting.DARK -> true
        ThemeSetting.SYSTEM -> isSystemInDarkTheme()
    }

    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (useDarkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        useDarkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            if (useDarkTheme) {
                window.statusBarColor = DarkColorScheme.background.toArgb()
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
            } else {
                window.statusBarColor = LightColorScheme.background.toArgb()
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = true
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = AppShapes,
        content = content
    )
}