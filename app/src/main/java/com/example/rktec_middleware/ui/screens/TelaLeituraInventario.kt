package com.example.rktec_middleware.ui.screens

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
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
import com.example.rktec_middleware.data.model.ItemInventario
import com.example.rktec_middleware.data.model.EpcTag
import com.example.rktec_middleware.viewmodel.RfidViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun TelaLeituraInventario(
    onVoltar: () -> Unit,
    banco: AppDatabase,
    listaFiltrada: List<ItemInventario>,
    listaTotal: List<ItemInventario>,
    filtroLoja: String?,
    filtroSetor: String?
) {
    val viewModel = androidx.lifecycle.viewmodel.compose.viewModel<RfidViewModel>()
    val tags by viewModel.tagList.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(top = 32.dp, bottom = 24.dp)
    ) {
        // CABEÇALHO - gradiente + botão voltar + título
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
        ) {
            IconButton(
                onClick = onVoltar,
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 8.dp, top = 32.dp)
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
                "INVENTÁRIO",
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(top = 32.dp)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            "Pressione o gatilho para ler",
            fontWeight = FontWeight.Bold,
            color = Color(0xFF4A90E2),
            fontSize = 18.sp,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            "Total de tags lidas: ${tags.size}",
            fontSize = 16.sp,
            color = Color.DarkGray,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Card(
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 12.dp)
        ) {
            if (tags.isEmpty()) {
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
                    items(tags) { tag ->
                        val status = statusTag(
                            tag.epc,
                            filtroLoja,
                            filtroSetor,
                            listaFiltrada,
                            listaTotal
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = when (status) {
                                    "verde" -> Icons.Filled.CheckCircle
                                    "amarelo" -> Icons.Filled.Warning
                                    "cinza" -> Icons.Filled.Info
                                    else -> Icons.Filled.Error
                                },
                                contentDescription = "Status",
                                tint = when (status) {
                                    "verde" -> Color(0xFF2E7D32)
                                    "amarelo" -> Color(0xFFFFC107)
                                    "cinza" -> Color.Gray
                                    else -> Color.Red
                                },
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                tag.epc,
                                fontSize = 16.sp,
                                color = when (status) {
                                    "verde" -> Color(0xFF2E7D32)
                                    "amarelo" -> Color(0xFFFFA000)
                                    "cinza" -> Color.Gray
                                    else -> Color.Red
                                },
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = status.replaceFirstChar { it.uppercase() },
                                color = Color.DarkGray,
                                fontSize = 14.sp
                            )
                        }
                        Divider()
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                CoroutineScope(Dispatchers.IO).launch {
                    banco.coletaDao().inserirTodos(tags)
                }
                // Você pode chamar onVoltar() aqui se quiser fechar a tela
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(horizontal = 8.dp),
            shape = RoundedCornerShape(20.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4A90E2))
        ) {
            Text("Finalizar Inventário", fontSize = 20.sp, color = Color.White)
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}
/**
 * Determina o status visual de cada tag lida, conforme o filtro ativo e inventário completo
 */
fun statusTag(
    epc: String,
    filtroLoja: String?,
    filtroSetor: String?,
    listaFiltrada: List<ItemInventario>,
    listaTotal: List<ItemInventario>
): String {
    // VERDE: Está no filtro (loja e setor selecionados)
    if (listaFiltrada.any { it.tag == epc }) return "verde"

    // AMARELO: Está na loja filtrada, mas em outro setor
    if (filtroLoja != null && listaTotal.any { it.tag == epc && it.loja == filtroLoja }) return "amarelo"

    // CINZA: Está em outra loja ou setor da planilha
    if (listaTotal.any { it.tag == epc }) return "cinza"

    // VERMELHO: Não está na planilha
    return "vermelho"
}
