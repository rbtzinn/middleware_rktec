package com.example.rktec_middleware.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.rktec_middleware.data.db.AppDatabase
import com.example.rktec_middleware.data.model.EpcTag
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TelaDebug(
    banco: AppDatabase,
    refresh: Int,
    onVoltar: () -> Unit,
    onBancoLimpo: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var epcs by remember { mutableStateOf<List<EpcTag>>(emptyList()) }
    var mostrarDialogLimparBanco by remember { mutableStateOf(false) }

    LaunchedEffect(refresh) {
        scope.launch {
            epcs = banco.coletaDao().listarTodas()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // Cabeçalho
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
                "DEBUG",
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.Center)
            )

            TextButton(
                onClick = { mostrarDialogLimparBanco = true },
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 10.dp)
            ) {
                Text("Limpar Banco", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Card central
        Card(
            shape = RoundedCornerShape(18.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 7.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp)
            ) {
                Text(
                    "Total de tags: ${epcs.size}",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(10.dp))

                if (epcs.isEmpty()) {
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Nenhuma tag salva ainda.", color = Color.Gray)
                    }
                } else {
                    LazyColumn {
                        items(epcs) { tag ->
                            Column(
                                Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                            ) {
                                Text(
                                    text = "${tag.epc} - ${formatarData(tag.timestamp)}",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                if (tag.descricao.isNotBlank()) {
                                    Text(
                                        text = tag.descricao,
                                        fontSize = 13.sp,
                                        color = Color(0xFF4A90E2),
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                                if (tag.setor.isNotBlank()) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(top = 2.dp, bottom = 2.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .background(Color(0xFFE1F5FE), shape = RoundedCornerShape(8.dp))
                                                .padding(horizontal = 8.dp, vertical = 3.dp)
                                        ) {
                                            Text(
                                                text = "Setor: ${tag.setor}",
                                                color = Color(0xFF0277BD),
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.SemiBold,
                                            )
                                        }
                                    }
                                }
                                if (tag.loja?.isNotBlank() == true) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(top = 2.dp, bottom = 2.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .background(Color(0xFFD1F5D5), shape = RoundedCornerShape(8.dp))
                                                .padding(horizontal = 8.dp, vertical = 3.dp)
                                        ) {
                                            Text(
                                                text = "Loja: ${tag.loja}",
                                                color = Color(0xFF2E7D32),
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.SemiBold,
                                            )
                                        }
                                    }
                                }
                                Divider()
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Dialog de confirmação
        if (mostrarDialogLimparBanco) {
            AlertDialog(
                onDismissRequest = { mostrarDialogLimparBanco = false },
                title = {
                    Text(
                        "⚠️ Atenção: Limpar TODOS os dados!",
                        color = Color.Red,
                        fontWeight = FontWeight.ExtraBold
                    )
                },
                text = {
                    Text(
                        "Isso vai apagar o inventário, as tags coletadas e o mapeamento de colunas!\n\nEssa ação NÃO pode ser desfeita.",
                        color = Color.DarkGray,
                        fontWeight = FontWeight.Bold
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            mostrarDialogLimparBanco = false
                            scope.launch {
                                banco.inventarioDao().limparInventario()
                                banco.coletaDao().limparColetas()
                                banco.mapeamentoDao().deletarTudo()
                                epcs = emptyList()
                                onBancoLimpo() // CHAMA O CALLBACK AQUI!
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                    ) {
                        Text("APAGAR TUDO", color = Color.White)
                    }
                },
                dismissButton = {
                    OutlinedButton(
                        onClick = { mostrarDialogLimparBanco = false }
                    ) {
                        Text("Cancelar")
                    }
                }
            )
        }
    }
}

fun formatarData(timestamp: Long): String {
    val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
    return sdf.format(Date(timestamp))
}
