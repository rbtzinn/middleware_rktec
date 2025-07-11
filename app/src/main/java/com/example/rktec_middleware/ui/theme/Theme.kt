package com.example.rktec_middleware.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.White
import androidx.compose.ui.platform.LocalContext

private val LightColorScheme = lightColorScheme(
    primary = AzulRKTEC,
    onPrimary = White,
    primaryContainer = AzulContainer,
    onPrimaryContainer = DarkText,
    background = White,
    surface = White,
    onBackground = DarkText,
    onSurface = DarkText,
    outline = Color(0xFFB0BEC5)
)

private val DarkColorScheme = darkColorScheme(
    primary = AzulRKTEC,
    onPrimary = White,
    background = Color(0xFF121212),
    surface = Color(0xFF1E1E1E),
    onBackground = White,
    onSurface = White
)
@Composable
fun RKTECmiddlewareTheme(content: @Composable () -> Unit) {
    val colorScheme = lightColorScheme(
        primary = Color(0xFF0077B5),
        onPrimary = Color.White,
        primaryContainer = Color(0xFF5599CC),
        onPrimaryContainer = Color.White,
        background = Color.White, // <- FUNDO CLARO
        onBackground = Color.Black,
        surface = Color.White,     // <- CARDS CLAROS
        onSurface = Color.Black,
        surfaceVariant = Color(0xFFF2F2F2),
        onSurfaceVariant = Color.DarkGray,
        outline = Color(0xFFCCCCCC),
    )

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography(),
        shapes = Shapes(), // ou defina shapes custom se necessário
        content = content
    )
}
