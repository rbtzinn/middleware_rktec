// ui/theme/Theme.kt
package com.example.rktec_middleware.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// PALETA DE CORES CLARAS USANDO NOSSAS CORES
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
    onSurfaceVariant = RktTextSecondary
)

@Composable
fun RKTecMiddlewareTheme( // Renomeado para consistência
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = LightColorScheme // Usando nossa paleta

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primaryContainer.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        shapes = AppShapes,
        content = content
    )
}