// ui/screens/TelaLeituraInventario.kt
package com.example.rktec_middleware.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.rktec_middleware.data.db.AppDatabase
import com.example.rktec_middleware.data.model.ItemInventario
import com.example.rktec_middleware.ui.components.PrimaryButton
import com.example.rktec_middleware.ui.theme.*
import com.example.rktec_middleware.util.LogHelper
import com.example.rktec_middleware.viewmodel.RfidViewModel
import com.example.rktec_middleware.viewmodel.RfidViewModelFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

private enum class StatusInventario {
    VERDE, AMARELO, CINZA, VERMELHO, CORRIGIDO
}

// ALTERAÇÃO: Adicionada a função de normalização que antes estava na outra tela.
private fun normalizarNome(nome: String): String {
    return nome
        .replace("\"", "")
        .trim()
        .uppercase()
}

@Composable
private fun StatusInventario.toColor(): Color = when (this) {
    StatusInventario.VERDE -> RktGreen
    StatusInventario.AMARELO -> RktYellow
    StatusInventario.CINZA -> RktTextSecondary
    StatusInventario.VERMELHO -> MaterialTheme.colorScheme.error
    StatusInventario.CORRIGIDO -> RktBlueInfo
}

@Composable
private fun StatusInventario.toIcon() = when (this) {
    StatusInventario.VERDE -> Icons.Default.CheckCircle
    StatusInventario.AMARELO -> Icons.Default.Warning
    StatusInventario.CINZA -> Icons.Default.Info
    StatusInventario.VERMELHO -> Icons.Default.Error
    StatusInventario.CORRIGIDO -> Icons.Default.PublishedWithChanges
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TelaLeituraInventario(
    onVoltar: () -> Unit,
    banco: AppDatabase,
    usuarioLogado: String,
    filtroLoja: String?,
    filtroSetor: String?
) {

    val context = LocalContext.current
    val viewModelFactory = RfidViewModelFactory(context)
    val viewModel: RfidViewModel = viewModel(factory = viewModelFactory)
    val tagsLidas by viewModel.tagList.collectAsState()
    val scope = rememberCoroutineScope()
    val tagsCorrigidasNaSessao = remember { mutableStateListOf<String>() }

    // ALTERAÇÃO: As listas agora são estados locais desta tela.
    var listaFiltrada by remember { mutableStateOf<List<ItemInventario>>(emptyList()) }
    var listaTotal by remember { mutableStateOf<List<ItemInventario>>(emptyList()) }

    // ALTERAÇÃO: Carrega os dados do banco de dados em segundo plano quando a tela abre.
    LaunchedEffect(filtroLoja, filtroSetor) {
        launch(Dispatchers.IO) {
            val todosOsItens = banco.inventarioDao().listarTodos()
            listaTotal = todosOsItens

            // Filtra a lista com base nos filtros recebidos
            listaFiltrada = todosOsItens.filter { item ->
                (filtroLoja.isNullOrEmpty() || normalizarNome(item.loja) == filtroLoja) &&
                        (filtroSetor.isNullOrEmpty() || item.localizacao.trim() == filtroSetor)
            }
        }
    }


    RKTecMiddlewareTheme {
        Scaffold(
            topBar = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp) // Altura padrão para um cabeçalho
                        .background(
                            Brush.verticalGradient(
                                0f to MaterialTheme.colorScheme.primaryContainer,
                                1f to MaterialTheme.colorScheme.primary
                            )
                        )
                )
            },
            containerColor = MaterialTheme.colorScheme.background
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize()
                    .padding(bottom = Dimens.PaddingMedium),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(Dimens.PaddingMedium))
                Text(
                    "Pressione o gatilho para ler",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(Dimens.PaddingSmall))
                // Agora este contador vai funcionar corretamente!
                Text(
                    "Lidos: ${tagsLidas.size} / Esperado: ${listaFiltrada.size}",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(Dimens.PaddingMedium))

                Card(
                    shape = MaterialTheme.shapes.medium,
                    elevation = CardDefaults.cardElevation(defaultElevation = Dimens.PaddingExtraSmall),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = Dimens.PaddingMedium)
                ) {
                    if (tagsLidas.isEmpty()) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("Nenhuma etiqueta lida ainda", color = RktTextSecondary)
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(Dimens.PaddingSmall),
                            verticalArrangement = Arrangement.spacedBy(Dimens.PaddingSmall)
                        ) {
                            items(tagsLidas, key = { it.epc }) { tag ->
                                val status = statusTag(tag.epc, filtroLoja, filtroSetor, listaFiltrada, listaTotal)

                                if (status == StatusInventario.AMARELO && filtroSetor != null && tag.epc !in tagsCorrigidasNaSessao) {
                                    LaunchedEffect(tag.epc) {
                                        scope.launch(Dispatchers.IO) {
                                            banco.inventarioDao().corrigirSetor(tag.epc, filtroSetor)
                                            tagsCorrigidasNaSessao.add(tag.epc)
                                        }
                                    }
                                }

                                val itemFoiCorrigido = tag.epc in tagsCorrigidasNaSessao
                                val statusFinal = if (itemFoiCorrigido) StatusInventario.CORRIGIDO else status

                                TagLidaItem(epc = tag.epc, status = statusFinal)
                                Divider(color = RktBackground)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(Dimens.PaddingMedium))

                PrimaryButton(
                    onClick = {
                        scope.launch {
                            val arquivoLog = LogHelper.registrarSessaoDeInventario(
                                context = context, usuario = usuarioLogado, loja = filtroLoja,
                                setor = filtroSetor, itensEsperados = listaFiltrada,
                                itensLidos = tagsLidas, itensTotaisDaBase = listaTotal
                            )
                            Toast.makeText(
                                context,
                                if (arquivoLog != null) "Sessão registrada no log: ${arquivoLog.name}"
                                else "Falha ao registrar a sessão.",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                        onVoltar()
                    },
                    text = "Finalizar Sessão",
                    modifier = Modifier.padding(horizontal = Dimens.PaddingMedium)
                )
            }
        }
    }
}

@Composable
private fun TagLidaItem(epc: String, status: StatusInventario) {
    val statusColor = status.toColor()
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = Dimens.PaddingExtraSmall, horizontal = Dimens.PaddingSmall)
    ) {
        Icon(
            imageVector = status.toIcon(),
            contentDescription = "Status",
            tint = statusColor,
            modifier = Modifier.size(Dimens.IconSizeSmall)
        )
        Spacer(modifier = Modifier.width(Dimens.PaddingMedium))
        Text(
            text = epc,
            style = MaterialTheme.typography.bodyLarge,
            color = statusColor,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = status.name.lowercase().replaceFirstChar { it.uppercase() },
            style = MaterialTheme.typography.bodyMedium
        )
    }
}


private fun statusTag(
    epc: String,
    filtroLoja: String?,
    filtroSetor: String?,
    listaFiltrada: List<ItemInventario>,
    listaTotal: List<ItemInventario>
): StatusInventario {
    // Agora a 'listaFiltrada' e 'listaTotal' são as que foram carregadas dentro desta tela
    if (listaFiltrada.any { it.tag == epc }) return StatusInventario.VERDE
    if (filtroLoja != null && listaTotal.any { it.tag == epc && normalizarNome(it.loja) == filtroLoja }) return StatusInventario.AMARELO
    if (listaTotal.any { it.tag == epc }) return StatusInventario.CINZA
    return StatusInventario.VERMELHO
}