package com.example.rktec_middleware.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.filled.ManageAccounts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight

@Composable
fun TelaPrincipal(
    onColetaClick: () -> Unit,
    onInventarioClick: () -> Unit,
    onDebugClick: () -> Unit,
    onSobreClick: () -> Unit,
    nomeUsuario: String,
    onSairClick: () -> Unit,
    onGerenciarUsuariosClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F7FA))
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Header estilizado
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .background(
                        Brush.verticalGradient(
                            0f to Color(0xFF174D86),
                            1f to Color(0xFF4A90E2)
                        )
                    )
            ) {
                Row(
                    Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp, vertical = 20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.17f)),
                        contentAlignment = Alignment.Center
                    ) {
                        // Avatar Placeholder (futuro: imagem do usuário)
                        Text(
                            text = nomeUsuario.take(2).uppercase(),
                            color = Color.White,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Black,
                            modifier = Modifier.clip(CircleShape)
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            "Bem-vindo!",
                            color = Color.White.copy(alpha = 0.9f),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            nomeUsuario,
                            color = Color.White,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    IconButton(
                        onClick = onSairClick,
                        modifier = Modifier
                            .size(44.dp)
                            .background(Color.White.copy(alpha = 0.20f), CircleShape)
                    ) {
                        Icon(
                            Icons.Filled.ExitToApp,
                            contentDescription = "Sair",
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }

            // Cards das ações principais
            Column(
                Modifier
                    .fillMaxWidth()
                    .offset(y = (-30).dp)
                    .padding(horizontal = 20.dp)
            ) {
                FeatureCard(
                    title = "Coleta",
                    subtitle = "Ler etiquetas RFID em tempo real",
                    icon = Icons.Filled.PlayCircle,
                    color = Color(0xFF4A90E2),
                    onClick = onColetaClick
                )
                Spacer(modifier = Modifier.height(18.dp))
                FeatureCard(
                    title = "Inventário",
                    subtitle = "Controle e acompanhe o estoque",
                    icon = Icons.Filled.ListAlt,
                    color = Color(0xFF45B37B),
                    onClick = onInventarioClick
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // Opções avançadas
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 0.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                TextButton(
                    onClick = onDebugClick,
                    modifier = Modifier.padding(vertical = 8.dp)
                ) {
                    Text(
                        "Debug - banco de dados",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                }
            }

            // Footer minimalista
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Divider(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color(0xFFEEEEEE),
                    thickness = 1.dp
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TextButton(onClick = onSobreClick) {
                        Icon(
                            imageVector = Icons.Filled.Info,
                            contentDescription = "Sobre o Sistema",
                            tint = Color(0xFF009688),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            "Sobre o Sistema",
                            fontSize = 14.sp,
                            color = Color(0xFF009688),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    "RKTECNOLOGIAS",
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF009688),
                    fontSize = 16.sp
                )
                Text(
                    "Todos os direitos reservados",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
        }

        // --- Floating Action Button para Gerenciar Usuários ---
        FloatingActionButton(
            onClick = onGerenciarUsuariosClick,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 24.dp, bottom = 36.dp),
            containerColor = Color(0xFF174D86)
        ) {
            Icon(
                imageVector = Icons.Filled.ManageAccounts,
                contentDescription = "Gerenciar Usuários",
                tint = Color.White
            )
        }
    }
}

@Composable
fun FeatureCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 5.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(94.dp)
            .shadow(6.dp, RoundedCornerShape(20.dp), clip = false)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 20.dp, end = 10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                color.copy(alpha = 0.13f),
                                color.copy(alpha = 0.32f)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon,
                    contentDescription = title,
                    tint = color,
                    modifier = Modifier.size(34.dp)
                )
            }
            Spacer(modifier = Modifier.width(22.dp))
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = Color(0xFF222222)
                )
                Text(
                    text = subtitle,
                    fontSize = 13.sp,
                    color = Color(0xFF757575)
                )
            }
        }
    }
}
