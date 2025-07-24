package com.example.rktec_middleware.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.example.rktec_middleware.ui.theme.Dimens
import com.example.rktec_middleware.ui.theme.LocalThemeIsDark // <-- Importa o nosso "canal"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GradientHeader(
    title: String,
    onVoltar: () -> Unit,
    actions: @Composable RowScope.() -> Unit = {}
) {
    // CORREÇÃO: Pega o valor anunciado pelo tema. Não usa mais '.brightness'.
    val isDarkTheme = LocalThemeIsDark.current

    val headerBrush = if (isDarkTheme) {
        // Um gradiente mais sutil para o tema escuro
        Brush.verticalGradient(
            0f to MaterialTheme.colorScheme.surface,
            1f to MaterialTheme.colorScheme.background
        )
    } else {
        // O gradiente azul original para o tema claro
        Brush.verticalGradient(
            0f to MaterialTheme.colorScheme.primaryContainer,
            1f to MaterialTheme.colorScheme.primary
        )
    }

    TopAppBar(
        title = { Text(text = title) },
        modifier = Modifier.background(headerBrush),
        navigationIcon = {
            IconButton(onClick = onVoltar) {
                Icon(
                    Icons.Default.ArrowBack,
                    contentDescription = "Voltar",
                    modifier = Modifier.size(Dimens.IconSizeLarge)
                )
            }
        },
        actions = actions,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Transparent,
            titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            actionIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
    )
}