package com.example.rktec_middleware.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.draw.shadow
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

    // Para edição:
    var tagEditando by remember { mutableStateOf<EpcTag?>(null) }
    var campoDesc by remember { mutableStateOf("") }
    var campoSetor by remember { mutableStateOf("") }
    var campoLoja by remember { mutableStateOf("") }

    LaunchedEffect(refresh) {
        scope.launch {
            epcs = banco.coletaDao().listarTodas()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F7FA))
    ) {
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
                onClick = onVoltar,
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 8.dp)
                    .size(48.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.ArrowBack,
                    contentDescription = "Voltar",
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }
            Text(
                "DEBUG",
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        Spacer(modifier = Modifier.height(18.dp))

        // CARD CENTRAL de estatística e lista
        Card(
            shape = RoundedCornerShape(18.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 7.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .weight(1f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp)
            ) {
                Text(
                    "Total de tags: ${epcs.size}",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF174D86)
                )
                Spacer(modifier = Modifier.height(12.dp))

                if (epcs.isEmpty()) {
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .padding(30.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Nenhuma tag salva ainda.", color = Color(0xFFB0BEC5), fontSize = 15.sp)
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxHeight()
                    ) {
                        items(epcs) { tag ->
                            Card(
                                Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 3.dp, horizontal = 2.dp)
                                    .clickable {
                                        tagEditando = tag
                                        campoDesc = tag.descricao
                                        campoSetor = tag.setor
                                        campoLoja = tag.loja ?: ""
                                    },
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Column(Modifier.padding(12.dp)) {
                                    Text(
                                        text = "${tag.epc}",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF222222),
                                        maxLines = 1
                                    )
                                    Text(
                                        text = formatarData(tag.timestamp),
                                        fontSize = 12.sp,
                                        color = Color(0xFF6C757D)
                                    )
                                    if (tag.descricao.isNotBlank()) {
                                        Text(
                                            text = tag.descricao,
                                            fontSize = 13.sp,
                                            color = Color(0xFF4A90E2),
                                            fontWeight = FontWeight.Medium,
                                            maxLines = 2
                                        )
                                    }
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        modifier = Modifier.padding(top = 3.dp, bottom = 2.dp)
                                    ) {
                                        if (tag.setor.isNotBlank()) {
                                            InfoChipDebug("Setor: ${tag.setor}", Color(0xFFE1F5FE), Color(0xFF0277BD))
                                        }
                                        if (tag.loja?.isNotBlank() == true) {
                                            InfoChipDebug("Loja: ${tag.loja}", Color(0xFFD1F5D5), Color(0xFF2E7D32))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Botão Limpar Banco
        Button(
            onClick = { mostrarDialogLimparBanco = true },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp)
                .height(54.dp),
            shape = RoundedCornerShape(22.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
            elevation = ButtonDefaults.buttonElevation(7.dp)
        ) {
            Text(
                "Limpar Banco",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Dialog de edição de item
        if (tagEditando != null) {
            AlertDialog(
                onDismissRequest = { tagEditando = null },
                title = { Text("Editar Tag", fontWeight = FontWeight.Bold) },
                text = {
                    Column {
                        OutlinedTextField(
                            value = campoDesc,
                            onValueChange = { campoDesc = it },
                            label = { Text("Descrição") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = campoSetor,
                            onValueChange = { campoSetor = it },
                            label = { Text("Setor") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = campoLoja,
                            onValueChange = { campoLoja = it },
                            label = { Text("Loja") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(onClick = {
                        scope.launch {
                            tagEditando?.let {
                                val atualizado = it.copy(
                                    descricao = campoDesc,
                                    setor = campoSetor,
                                    loja = campoLoja
                                )
                                banco.coletaDao().atualizarTag(atualizado)
                                epcs = banco.coletaDao().listarTodas()
                                tagEditando = null
                            }
                        }
                    }) {
                        Text("Salvar")
                    }
                },
                dismissButton = {
                    OutlinedButton(
                        onClick = { tagEditando = null },
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = Color.White,
                            contentColor = Color(0xFFD32F2F)
                        ),
                        border = ButtonDefaults.outlinedButtonBorder.copy(width = 1.5.dp, brush = Brush.horizontalGradient(listOf(Color(0xFFD32F2F), Color(0xFF4A90E2))))
                    ) {
                        Text("Cancelar", fontWeight = FontWeight.Bold)
                    }
                }
            )
        }

        // Dialog de confirmação
        if (mostrarDialogLimparBanco) {
            AlertDialog(
                onDismissRequest = { mostrarDialogLimparBanco = false },
                title = {
                    Text(
                        "⚠️ Atenção: Limpar TODOS os dados!",
                        color = Color(0xFFD32F2F),
                        fontWeight = FontWeight.ExtraBold
                    )
                },
                text = {
                    Text(
                        "Isso vai apagar o inventário, as tags coletadas e o mapeamento de colunas!\n\nEssa ação NÃO pode ser desfeita.",
                        color = Color(0xFF424242),
                        fontWeight = FontWeight.Medium
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
                                onBancoLimpo()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text("APAGAR TUDO", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    OutlinedButton(
                        onClick = { mostrarDialogLimparBanco = false },
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = Color.White,
                            contentColor = Color(0xFFD32F2F)
                        ),
                        border = ButtonDefaults.outlinedButtonBorder.copy(width = 1.5.dp, brush = Brush.horizontalGradient(listOf(Color(0xFFD32F2F), Color(0xFF4A90E2))))
                    ) {
                        Text("Cancelar", fontWeight = FontWeight.Bold)
                    }
                }
            )
        }
    }
}

@Composable
fun InfoChipDebug(text: String, bgColor: Color, fgColor: Color) {
    Box(
        modifier = Modifier
            .background(bgColor, shape = RoundedCornerShape(8.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = text,
            color = fgColor,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

fun formatarData(timestamp: Long): String {
    val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
    return sdf.format(Date(timestamp))
}


