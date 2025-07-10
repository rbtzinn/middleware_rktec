package com.example.rktec_middleware.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun TelaSobre(onVoltar: () -> Unit) {
    // Cores padrão RKTEC
    val mainBlue = Color(0xFF4A90E2)
    val darkBlue = Color(0xFF174D86)
    val lightBg = Color(0xFFF7FAFC)

    var expanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(lightBg)
    ) {
        // CABEÇALHO LUXO
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp)
                .background(
                    Brush.horizontalGradient(listOf(mainBlue, darkBlue))
                )
                .shadow(6.dp, RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
        ) {
            IconButton(
                onClick = onVoltar,
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 14.dp)
                    .size(54.dp)
                    .background(Color.White.copy(alpha = 0.12f), shape = CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Filled.ArrowBack,
                    contentDescription = "Voltar",
                    tint = Color.White,
                    modifier = Modifier.size(34.dp)
                )
            }
            Text(
                text = "Sobre o App",
                color = Color.White,
                fontSize = 26.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.4.sp,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        Spacer(modifier = Modifier.height(36.dp))

        // CARD CENTRAL (com FAQ em accordion)
        Card(
            shape = RoundedCornerShape(30.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(12.dp),
            modifier = Modifier
                .padding(horizontal = 18.dp)
                .fillMaxWidth()
                .shadow(6.dp, RoundedCornerShape(30.dp))
        ) {
            Column(
                modifier = Modifier
                    .padding(vertical = 28.dp, horizontal = 20.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    shape = CircleShape,
                    color = mainBlue.copy(alpha = 0.10f),
                    modifier = Modifier.size(68.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Info,
                        contentDescription = "Logo App",
                        tint = mainBlue,
                        modifier = Modifier
                            .padding(16.dp)
                            .size(38.dp)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "RKTEC Middleware",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = darkBlue,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "App universal para leitura de etiquetas RFID, controle de inventário e integração com planilhas.\n\nSeu inventário digital, sem complicação.",
                    fontSize = 17.sp,
                    color = Color(0xFF374151),
                    textAlign = TextAlign.Center,
                    lineHeight = 23.sp
                )
                Spacer(modifier = Modifier.height(22.dp))
                Divider(thickness = 1.1.dp, color = mainBlue.copy(alpha = 0.18f))
                Spacer(modifier = Modifier.height(18.dp))
                Text(
                    text = "Desenvolvido por Roberto Gabriel, Kawã Vinícius e equipe RKTEC Tecnologias.",
                    fontSize = 16.sp,
                    color = mainBlue,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )
                Spacer(modifier = Modifier.height(13.dp))
                Text(
                    text = "Versão 1.0.0",
                    fontSize = 14.sp,
                    color = Color.Gray,
                    fontWeight = FontWeight.Light
                )

                Spacer(modifier = Modifier.height(26.dp))

                // FAQ/Accordion - Como Usar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            mainBlue.copy(alpha = 0.06f),
                            RoundedCornerShape(18.dp)
                        )
                        .clickable { expanded = !expanded }
                        .padding(0.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 14.dp, horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Como usar?",
                            fontSize = 18.sp,
                            color = darkBlue,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f)
                        )
                        Icon(
                            imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = if (expanded) "Recolher" else "Expandir",
                            tint = mainBlue,
                            modifier = Modifier.size(30.dp)
                        )
                    }
                }
                AnimatedVisibility(
                    visible = expanded,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White)
                            .padding(horizontal = 14.dp, vertical = 10.dp)
                    ) {
                        Text(
                            text = """
                                1. Importe sua planilha de inventário na tela inicial.
                                2. Clique em "Iniciar Leitura" para começar a captar etiquetas RFID.
                                3. Compare os EPCs lidos com os cadastrados e visualize os status por cor.
                                4. Exporte os resultados em Excel ou CSV a qualquer momento.
                                5. Consulte o histórico e gerencie os dados pelo app.
                                
                                Dica: Toque nos setores e lojas para filtrar a leitura por ambiente.
                                """.trimIndent(),
                            fontSize = 16.sp,
                            color = Color(0xFF374151),
                            lineHeight = 21.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Copyright
        Text(
            text = "© 2024 RKTEC Tecnologias",
            fontSize = 15.sp,
            color = mainBlue,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(bottom = 22.dp)
        )
    }
}
