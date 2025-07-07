@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.rktec_middleware.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Nfc
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.draw.shadow
import com.example.rktec_middleware.viewmodel.RfidViewModel
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import androidx.compose.runtime.saveable.rememberSaveable

@Composable
fun TelaLeituraColeta(
    viewModel: RfidViewModel,
    onVoltar: () -> Unit
) {
    val systemUiController = rememberSystemUiController()
    SideEffect {
        systemUiController.setStatusBarColor(Color(0xFF4A90E2))
    }

    val tagList by viewModel.tagList.collectAsState()
    var lendo by remember { mutableStateOf(false) }
    var isRefreshing by remember { mutableStateOf(false) }
    var isDialogVoltarAberto by remember { mutableStateOf(false) }
    var isDialogLimparAberto by remember { mutableStateOf(false) }
    var naoMostrarLimpar by rememberSaveable { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F7FA))
    ) {
        // CABEÇALHO padrão
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(110.dp)
                .background(
                    Brush.verticalGradient(
                        0f to Color(0xFF174D86),
                        1f to Color(0xFF4A90E2)
                    )
                )
                .shadow(3.dp)
        ) {
            IconButton(
                onClick = { isDialogVoltarAberto = true },
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 8.dp)
                    .size(48.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.ArrowBack,
                    contentDescription = "Voltar",
                    tint = Color.White,
                    modifier = Modifier.size(30.dp)
                )
            }
            Text(
                "COLETA",
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .align(Alignment.Center)
            )
            IconButton(
                onClick = {
                    if (naoMostrarLimpar) {
                        viewModel.limparTags()
                    } else {
                        isDialogLimparAberto = true
                    }
                },
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 8.dp)
                    .size(48.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = "Limpar tags",
                    tint = Color.White,
                    modifier = Modifier.size(26.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Mensagem de leitura
        Text(
            if (lendo) "Lendo etiquetas RFID..." else "Pressione o gatilho do PDA para ler",
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF4A90E2),
            fontSize = 17.sp,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(2.dp))

        Text(
            "Total de tags lidas: ${tagList.size}",
            fontSize = 15.sp,
            color = Color(0xFF666666),
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(10.dp))

        // LISTA DE TAGS
        SwipeRefresh(
            state = rememberSwipeRefreshState(isRefreshing),
            onRefresh = {
                isRefreshing = true
                isRefreshing = false
            },
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            Card(
                shape = RoundedCornerShape(18.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 14.dp, vertical = 2.dp)
            ) {
                if (tagList.isEmpty()) {
                    Box(
                        Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "Nenhuma etiqueta lida ainda",
                            color = Color(0xFFB0BEC5),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                } else {
                    LazyColumn(
                        Modifier
                            .fillMaxSize()
                            .padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(tagList) { tag ->
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F9FC)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .shadow(1.dp, RoundedCornerShape(12.dp))
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp, horizontal = 10.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .background(Color(0xFF4A90E2), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Nfc,
                                            contentDescription = "Tag",
                                            tint = Color.White,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        tag.epc,
                                        fontSize = 15.sp,
                                        color = Color(0xFF174D86),
                                        fontWeight = FontWeight.Medium,
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Botão Finalizar Coleta
        Button(
            onClick = { viewModel.limparTags() },
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .padding(horizontal = 20.dp),
            shape = RoundedCornerShape(24.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF4A90E2)
            ),
            elevation = ButtonDefaults.buttonElevation(8.dp)
        ) {
            Text("Finalizar Coleta", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }

        Spacer(modifier = Modifier.height(12.dp))
    }

    // Dialog de confirmação ao voltar
    if (isDialogVoltarAberto) {
        AlertDialog(
            onDismissRequest = { isDialogVoltarAberto = false },
            title = { Text("Confirmar saída", fontWeight = FontWeight.Bold) },
            text = { Text("Deseja realmente voltar? As tags lidas serão apagadas.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.limparTags()
                    isDialogVoltarAberto = false
                    naoMostrarLimpar = false
                    onVoltar()
                }) {
                    Text("Sim", color = Color(0xFF4A90E2), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { isDialogVoltarAberto = false }) {
                    Text("Não", color = Color.Gray)
                }
            }
        )
    }

    // Dialog de confirmação ao limpar
    if (isDialogLimparAberto) {
        AlertDialog(
            onDismissRequest = { isDialogLimparAberto = false },
            title = { Text("Confirmar limpeza", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Deseja realmente limpar as tags lidas?")
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = naoMostrarLimpar,
                            onCheckedChange = { naoMostrarLimpar = it }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Não mostrar esta mensagem novamente")
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.limparTags()
                    isDialogLimparAberto = false
                }) {
                    Text("Sim", color = Color(0xFF4A90E2), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { isDialogLimparAberto = false }) {
                    Text("Não", color = Color.Gray)
                }
            }
        )
    }
}
