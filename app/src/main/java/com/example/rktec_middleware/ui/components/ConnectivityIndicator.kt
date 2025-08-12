package com.example.rktec_middleware.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.rktec_middleware.ui.theme.RktGreen
import com.example.rktec_middleware.util.ConnectivityObserver

// SEU INDICADOR ORIGINAL (correto, sem alterações)
@Composable
fun ConnectivityStatusIndicator(
    status: ConnectivityObserver.Status
) {
    val isOnline = status == ConnectivityObserver.Status.Available
    val backgroundColor by animateColorAsState(
        targetValue = if (isOnline) RktGreen else Color.Gray,
        label = "backgroundColorAnimation"
    )
    val icon = if (isOnline) Icons.Default.Wifi else Icons.Default.CloudOff
    val text = if (isOnline) "Online" else "Offline"

    AnimatedVisibility(
        visible = true,
        enter = expandVertically(),
        exit = shrinkVertically()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(backgroundColor)
                .padding(horizontal = 16.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = "Status da Conexão",
                tint = Color.White,
                modifier = Modifier.size(16.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = text,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp
            )
        }
    }
}

// ===== NOVO INDICADOR SIMPLES (AGORA CORRIGIDO) =====
@Composable
fun SimpleConnectivityIndicator(status: ConnectivityObserver.Status) { // <-- TIPO CORRIGIDO
    val color = when (status) {
        ConnectivityObserver.Status.Available -> RktGreen // Usando sua cor RktGreen
        ConnectivityObserver.Status.Losing -> Color.Yellow
        // Trata Lost e Unavailable da mesma forma
        else -> Color.Gray
    }
    Box(
        modifier = Modifier
            .size(8.dp) // Tamanho do círculo
            .clip(CircleShape)
            .background(color)
    )
}