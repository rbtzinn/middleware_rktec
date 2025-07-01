package com.example.rktec_middleware.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Person
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.shape.RoundedCornerShape
import com.example.rktec_middleware.viewmodel.RfidViewModel
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.graphics.Brush


@Composable
fun TelaLeituraRfid(
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
        modifier = Modifier.fillMaxSize()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(90.dp)
                .background(
                    Brush.horizontalGradient(
                        listOf(Color(0xFF4A90E2), Color(0xFF174D86))
                    )
                )
        ) {
            IconButton(
                onClick = { isDialogVoltarAberto = true },
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 8.dp, top = 10.dp)
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
                "Leitura de Tags",
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(top = 10.dp)
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
                    .padding(end = 8.dp, top = 10.dp)
                    .size(48.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = "Limpar tags",
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            if (lendo) "Leitura sendo efetuada..." else "Pressione o gatilho para ler",
            fontWeight = FontWeight.Bold,
            color = Color(0xFF4A90E2),
            fontSize = 18.sp,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            "Total de tags lidas: ${tagList.size}",
            fontSize = 16.sp,
            color = Color.DarkGray,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(8.dp))

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
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp)
            ) {
                if (tagList.isEmpty()) {
                    Box(
                        Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Nenhuma etiqueta lida ainda", color = Color.Gray)
                    }
                } else {
                    LazyColumn(
                        Modifier
                            .fillMaxSize()
                            .padding(8.dp)
                    ) {
                        items(tagList) { tag ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Person,
                                    contentDescription = "Tag",
                                    tint = Color(0xFF4A90E2),
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(tag.epc, fontSize = 16.sp)
                            }
                            Divider()
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { viewModel.adicionarTagFake() },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(horizontal = 8.dp),
            shape = RoundedCornerShape(20.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4A90E2))
        ) {
            Text("Simular Leitura", fontSize = 20.sp, color = Color.White)
        }

        Spacer(modifier = Modifier.height(20.dp))
    }

    if (isDialogVoltarAberto) {
        AlertDialog(
            onDismissRequest = { isDialogVoltarAberto = false },
            title = { Text("Confirmar saída") },
            text = { Text("Deseja realmente voltar? As tags lidas serão apagadas.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.limparTags()
                    isDialogVoltarAberto = false
                    naoMostrarLimpar = false
                    onVoltar()
                }) {
                    Text("Sim")
                }
            },
            dismissButton = {
                TextButton(onClick = { isDialogVoltarAberto = false }) {
                    Text("Não")
                }
            }
        )
    }

    if (isDialogLimparAberto) {
        AlertDialog(
            onDismissRequest = { isDialogLimparAberto = false },
            title = { Text("Confirmar limpeza") },
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
                    Text("Sim")
                }
            },
            dismissButton = {
                TextButton(onClick = { isDialogLimparAberto = false }) {
                    Text("Não")
                }
            }
        )
    }
}
