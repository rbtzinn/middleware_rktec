package com.example.rktec_middleware.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.IntegrationInstructions
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.ImportantDevices
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


@Composable
fun TelaSobre(onVoltar: () -> Unit) {
    val gradient = Brush.verticalGradient(
        colors = listOf(Color(0xFF174D86), Color(0xFF4A90E2)),
        startY = 0f,
        endY = 1000f
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(88.dp)
                .background(
                    Brush.verticalGradient(
                        0f to Color(0xFF174D86),
                        1f to Color(0xFF4A90E2)
                    ),
                    shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp)
                )
                .shadow(16.dp, shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
        ) {
            IconButton(
                onClick = { onVoltar() },
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 16.dp)
                    .size(52.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.ArrowBack,
                    contentDescription = "Voltar",
                    tint = Color.White,
                    modifier = Modifier.size(36.dp)
                )
            }

            Text(
                "SOBRE O APLICATIVO",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.sp,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        // Conteúdo principal com scroll
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp)
                .padding(top = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                // Card com efeito de vidro (Glassmorphism)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color(0xAAFFFFFF),
                                    Color(0xBBE6F0FF)
                                )
                            ),
                            shape = RoundedCornerShape(32.dp)
                        )
                        .border(
                            1.dp,
                            Color(0x66FFFFFF),
                            RoundedCornerShape(32.dp)
                        )
                        .padding(24.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Logo/Ícone
                        Box(
                            modifier = Modifier
                                .size(100.dp)
                                .background(
                                    Color(0xFF174D86),
                                    CircleShape
                                )
                                .padding(16.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ImportantDevices,
                                contentDescription = "Logo",
                                tint = Color.White,
                                modifier = Modifier.size(48.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        Text(
                            "RKTEC MIDDLEWARE",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF0D3A6A),
                            letterSpacing = 1.2.sp
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            "Solução completa para gestão de ativos RFID",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF4A6580),
                            textAlign = TextAlign.Center,
                            lineHeight = 24.sp
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Divider(
                            color = Color(0x55174D86),
                            thickness = 1.dp,
                            modifier = Modifier.padding(horizontal = 32.dp)
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        FeatureItem(
                            icon = Icons.Default.Storage,
                            title = "Controle de Inventário",
                            description = "Leitura e gerenciamento de tags RFID em tempo real"
                        )

                        FeatureItem(
                            icon = Icons.Default.IntegrationInstructions,
                            title = "Integração com Planilhas",
                            description = "Exportação para Excel e Google Sheets"
                        )

                        FeatureItem(
                            icon = Icons.Default.CloudUpload,
                            title = "Cloud Sync",
                            description = "Sincronização segura com servidores na nuvem"
                        )

                        Spacer(modifier = Modifier.height(32.dp))

                        Text(
                            "Versão 1.0.0",
                            fontSize = 14.sp,
                            color = Color(0xFF6B8DB4),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Equipe de desenvolvimento
                TeamSection()
            }
        }

        // Footer com micro-interação
        Surface(
            color = Color(0x990D3A6A),
            tonalElevation = 8.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
        ) {
            Text(
                "© 2024 RKTEC TECNOLOGIAS | TODOS OS DIREITOS RESERVADOS",
                fontSize = 12.sp,
                color = Color(0xFFA3C5FF),
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .padding(vertical = 12.dp)
                    .fillMaxWidth()
                    .clickable { /* Abrir website */ }
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onPress = { /* Efeito de feedback */ },
                            onTap = { /* Ação */ }
                        )
                    }
            )
        }
    }
}

@Composable
fun FeatureItem(icon: ImageVector, title: String, description: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color(0xFF174D86),
            modifier = Modifier.size(28.dp)
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column {
            Text(
                title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0D3A6A)
            )
            Text(
                description,
                fontSize = 14.sp,
                color = Color(0xFF4A6580),
                lineHeight = 20.sp
            )
        }
    }
}

@Composable
fun TeamSection() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0x22000000), RoundedCornerShape(24.dp))
            .padding(24.dp)
    ) {
        Text(
            "DESENVOLVIDO POR",
            fontSize = 14.sp,
            color = Color.Black, // <-- Alterado pra preto
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        TeamMember("Roberto Gabriel", "Tech Lead")
        TeamMember("Kawã Vinicius", "Mobile Developer")
        TeamMember("Equipe RKTEC", "Engenharia & Design")
    }
}

@Composable
fun TeamMember(name: String, role: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(Color(0x55FFFFFF), CircleShape)
                .padding(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                tint = Color(0xFF174D86), // Azul institucional p/ contraste no branco
                modifier = Modifier.fillMaxSize()
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column {
            Text(
                name,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.Black // <-- Alterado pra preto
            )
            Text(
                role,
                fontSize = 14.sp,
                color = Color(0xFF174D86) // Azul institucional pra destacar
            )
        }
    }
}

