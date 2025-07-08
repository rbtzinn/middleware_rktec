package com.example.rktec_middleware.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.shape.RoundedCornerShape

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TelaEsqueciSenha(
    aoEnviarCodigo: (String) -> Unit,
    carregando: Boolean = false,
    mensagemErro: String? = null,
    aoVoltarLogin: () -> Unit // <-- novo parâmetro pra ação de voltar!
) {
    var email by remember { mutableStateOf("") }

    Column(
        Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F7FA))
    ) {
        // Header com seta de voltar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(110.dp)
                .background(
                    Brush.verticalGradient(
                        0f to Color(0xFF4A90E2),
                        1f to Color(0xFF174D86)
                    )
                )
                .shadow(3.dp)
        ) {
            IconButton(
                onClick = aoVoltarLogin,
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 8.dp, top = 16.dp)
                    .size(46.dp)
                    .background(Color.White.copy(alpha = 0.18f), CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Filled.ArrowBack,
                    contentDescription = "Voltar",
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }
            Text(
                "Recuperar Senha",
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        // Conteúdo central
        Column(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(Modifier.height(30.dp))
            Text(
                "Digite o seu e-mail para receber o link de redefinição de senha.",
                fontSize = 16.sp,
                color = Color(0xFF174D86),
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(bottom = 14.dp)
            )
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Seu e-mail") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            )
            Spacer(Modifier.height(22.dp))
            Button(
                onClick = { aoEnviarCodigo(email.trim()) },
                enabled = email.isNotBlank() && !carregando,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(22.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4A90E2))
            ) {
                Text(if (carregando) "Enviando..." else "Enviar código", fontSize = 16.sp)
            }
            mensagemErro?.let {
                Text(it, color = Color.Red, modifier = Modifier.padding(top = 10.dp))
            }
        }
    }
}
