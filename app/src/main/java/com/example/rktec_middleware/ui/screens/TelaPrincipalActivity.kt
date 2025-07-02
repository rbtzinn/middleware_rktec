package com.example.rktec_middleware

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.shape.RoundedCornerShape

@Composable
fun TelaPrincipal(
    onColetaClick: () -> Unit,
    onInventarioClick: () -> Unit,
    onDebugClick: () -> Unit,
    onSobreClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(top = 32.dp, bottom = 24.dp)
    ) {
        // Cabeçalho estiloso
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .offset(y = -32.dp)
                .background(
                    Brush.horizontalGradient(
                        listOf(Color(0xFF4A90E2), Color(0xFF174D86))
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "SMARTRACKER TECNOLOGIA RFID",
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 32.dp)
            )
        }

        // Lista de botões (LazyColumn)
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Button(
                    onClick = onColetaClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp),
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4A90E2))
                ) {
                    Text("Coleta", fontSize = 26.sp, color = Color.White)
                }
            }
            item {
                Button(
                    onClick = onInventarioClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp),
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4A90E2))
                ) {
                    Text("Inventário", fontSize = 26.sp, color = Color.White)
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Divider(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            color = Color(0xFFE0E0E0),
            thickness = 1.dp
        )

        // Footer simples e elegante
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        ) {
            TextButton(
                onClick = onDebugClick,
            ) {
                Text(
                    "Ver debug de coletas",
                    fontSize = 14.sp,
                    color = Color.Gray,
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            TextButton(
                onClick = onSobreClick, // Novo: abre a tela Sobre!
            ) {
                Text(
                    "RKTECNOLOGIAS",
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF009688),
                    fontSize = 16.sp
                )
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                "Todos os direitos reservados — RKTECNOLOGIAS",
                fontSize = 12.sp,
                color = Color.Gray,
            )
        }
    }
}


