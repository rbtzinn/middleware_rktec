package com.example.rktec_middleware.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun TelaSobre(onVoltar: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // Cabeçalho estiloso
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp)
                .background(
                    Brush.horizontalGradient(
                        listOf(Color(0xFF4A90E2), Color(0xFF174D86))
                    )
                )
        ) {
            IconButton(
                onClick = { onVoltar() },
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 8.dp)
                    .size(48.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.ArrowBack,
                    contentDescription = "Voltar",
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }

            Text(
                "SOBRE",
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Card estiloso central
        Card(
            shape = RoundedCornerShape(22.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(22.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "RKTEC Middleware",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF174D86)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "App universal para leitura de etiquetas RFID,\ncontrole de inventário, integração com planilhas e exportação de dados.",
                    fontSize = 16.sp,
                    color = Color.DarkGray,
                    lineHeight = 22.sp
                )
                Spacer(modifier = Modifier.height(18.dp))
                Divider()
                Spacer(modifier = Modifier.height(18.dp))
                Text(
                    "Desenvolvido por Roberto Gabriel, Kawã Vinicius\n e equipe RKTEC Tecnologias.",
                    fontSize = 16.sp,
                    color = Color(0xFF4A90E2),
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    "Versão 1.0.0",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Text(
            "© 2024 RKTEC Tecnologias",
            fontSize = 14.sp,
            color = Color(0xFF4A90E2),
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(bottom = 18.dp)
        )
    }
}
