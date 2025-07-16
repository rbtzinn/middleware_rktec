package com.example.rktec_middleware.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.rktec_middleware.ui.theme.Dimens

@Composable
fun GradientHeader(
    title: String,
    onVoltar: () -> Unit,
    actions: @Composable RowScope.() -> Unit = {}
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primaryContainer,
                        MaterialTheme.colorScheme.primary
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        // Ícone de Voltar
        IconButton(
            onClick = onVoltar,
            modifier = Modifier.align(Alignment.CenterStart).padding(start = Dimens.PaddingSmall)
        ) {
            Icon(
                Icons.Default.ArrowBack,
                contentDescription = "Voltar",
                tint = Color.White
            )
        }

        // Título Centralizado
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            color = Color.White
        )

        // Ícones de Ação (à direita)
        Row(
            modifier = Modifier.align(Alignment.CenterEnd).padding(end = Dimens.PaddingSmall),
            horizontalArrangement = Arrangement.spacedBy(Dimens.PaddingSmall),
            verticalAlignment = Alignment.CenterVertically
        ) {
            actions()
        }
    }
}