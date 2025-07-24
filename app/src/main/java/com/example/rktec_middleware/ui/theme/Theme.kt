package com.example.rktec_middleware.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.example.rktec_middleware.data.model.ThemeOption

// CORREÇÃO 1: Criar um "canal" para saber se o tema é escuro.
val LocalThemeIsDark = compositionLocalOf { false }

private val LightColorScheme = lightColorScheme(
    primary = RktBluePrimary,
    onPrimary = Color.White,
    primaryContainer = RktBlueDark,
    onPrimaryContainer = Color.White,
    secondary = RktBlueInfo,
    onSecondary = Color.White,
    tertiary = RktGreen,
    onTertiary = Color.White,
    error = RktRed,
    onError = Color.White,
    background = RktBackground,
    onBackground = RktTextPrimary,
    surface = RktSurface,
    onSurface = RktTextPrimary,
    surfaceVariant = RktBlueLight,
    onSurfaceVariant = RktTextSecondary,
    outline = RktStroke
)

private val DarkColorScheme = darkColorScheme(
    primary = RktBluePrimary,
    onPrimary = Color.White,
    primaryContainer = RktBlueDark,
    onPrimaryContainer = Color.White,
    secondary = RktBlueInfo,
    onSecondary = Color.White,
    tertiary = RktGreen,
    onTertiary = Color.White,
    error = RktRed,
    onError = Color.White,
    background = Color(0xFF121212),
    onBackground = Color.White,
    surface = Color(0xFF1E1E1E),
    onSurface = Color.White,
    surfaceVariant = Color(0xFF2C2B30),
    onSurfaceVariant = Color(0xFFCAC4D0),
    outline = RktStroke
)

@Composable
fun RKTecMiddlewareTheme(
    themeOption: ThemeOption = ThemeOption.SYSTEM,
    content: @Composable () -> Unit
) {
    val darkTheme = when (themeOption) {
        ThemeOption.LIGHT -> false
        ThemeOption.DARK -> true
        ThemeOption.SYSTEM -> isSystemInDarkTheme()
    }

    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primaryContainer.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    CompositionLocalProvider(LocalThemeIsDark provides darkTheme) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = AppTypography,
            shapes = AppShapes,
            content = content
        )
    }
}