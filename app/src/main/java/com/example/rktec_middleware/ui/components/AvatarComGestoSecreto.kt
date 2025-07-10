package com.example.rktec_middleware.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun AvatarComGestoSecreto(
    nomeUsuario: String,
    onGestoDetectado: () -> Unit
) {
    var tempoPressionado by remember { mutableStateOf(0L) }

    Box(
        modifier = Modifier
            .size(54.dp)
            .clip(CircleShape)
            .background(Color.White.copy(alpha = 0.17f))
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        val start = System.currentTimeMillis()
                        tempoPressionado = start

                        tryAwaitRelease()

                        val duracao = System.currentTimeMillis() - tempoPressionado
                        if (duracao >= 5000) { // 5 segundos
                            onGestoDetectado()
                        }
                    }
                )
            },
        contentAlignment = androidx.compose.ui.Alignment.Center
    ) {
        Text(
            text = nomeUsuario.take(2).uppercase(),
            color = Color.White,
            fontSize = 22.sp,
            fontWeight = FontWeight.Black
        )
    }
}
