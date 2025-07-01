package com.example.rktec_middleware.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.shape.RoundedCornerShape
import com.example.rktec_middleware.data.db.AppDatabase
import com.example.rktec_middleware.data.model.ItemInventario
import kotlinx.coroutines.launch

@Composable
fun TelaLeituraInventario(
    onVoltar: () -> Unit,
    banco: AppDatabase
) {
    val scope = rememberCoroutineScope()
    var lendo by remember { mutableStateOf(false) }
    var tagsLidas by remember { mutableStateOf<List<Pair<ItemInventario?, String>>>(emptyList()) }
    var statusMsg by remember { mutableStateOf<String?>(null) }
    var isDialogVoltarAberto by remember { mutableStateOf(false) }

    // Função de leitura (troca pelo trigger do PDA!)
    fun lerTag(tag: String) {
        lendo = true
        scope.launch {
            val item = banco.inventarioDao().buscarPorTag(tag)
            tagsLidas = tagsLidas + Pair(item, tag)
            statusMsg = if (item != null) "Item encontrado: ${item.descricao}" else "Item NÃO encontrado!"
            lendo = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(top = 32.dp, bottom = 24.dp)
    ) {
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
                "Leitura de Inventário",
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(top = 10.dp)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Status leitura
        Text(
            if (lendo) "Lendo tag..." else "Aguardando leitura do dispositivo",
            fontWeight = FontWeight.Bold,
            color = Color(0xFF4A90E2),
            fontSize = 18.sp,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(4.dp))
        Text(
            "Total de EPCs verificados: ${tagsLidas.size}",
            fontSize = 16.sp,
            color = Color.DarkGray,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Lista de resultados
        Card(
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 12.dp)
        ) {
            if (tagsLidas.isEmpty()) {
                Box(
                    Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Nenhuma tag inventariada ainda", color = Color.Gray)
                }
            } else {
                LazyColumn(
                    Modifier
                        .fillMaxSize()
                        .padding(8.dp)
                ) {
                    items(tagsLidas) { (item, tag) ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp)
                        ) {
                            Icon(
                                imageVector = if (item != null) Icons.Filled.CheckCircle else Icons.Filled.Cancel,
                                contentDescription = null,
                                tint = if (item != null) Color(0xFF2E7D32) else Color.Red
                            )
                            Spacer(Modifier.width(10.dp))
                            Text(tag, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Spacer(Modifier.width(10.dp))
                            Text(item?.descricao ?: "Não cadastrado", color = Color.Gray)
                        }
                        Divider()
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Simulador de leitura (substitua pelo gatilho do PDA)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(
                onClick = { lerTag("EPC001") },
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4A90E2))
            ) { Text("Simular EPC001", color = Color.White) }

            Button(
                onClick = { lerTag("ALEATORIO") },
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)
            ) { Text("Simular Tag Inválida", color = Color.White) }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }

    // Diálogo de confirmação para voltar
    if (isDialogVoltarAberto) {
        AlertDialog(
            onDismissRequest = { isDialogVoltarAberto = false },
            title = { Text("Confirmar saída") },
            text = { Text("Deseja realmente voltar? O progresso do inventário será perdido.") },
            confirmButton = {
                TextButton(onClick = {
                    isDialogVoltarAberto = false
                    onVoltar()
                }) { Text("Sim") }
            },
            dismissButton = {
                TextButton(onClick = { isDialogVoltarAberto = false }) { Text("Não") }
            }
        )
    }
}
