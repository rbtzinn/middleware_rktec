// ui/screens/TelaChecagem.kt
package com.example.rktec_middleware.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.rktec_middleware.data.db.AppDatabase
import com.example.rktec_middleware.data.model.ItemInventario
import com.example.rktec_middleware.ui.components.PrimaryButton
import com.example.rktec_middleware.ui.components.StandardTextField
import com.example.rktec_middleware.ui.theme.*
import com.example.rktec_middleware.viewmodel.RfidViewModel
import com.example.rktec_middleware.viewmodel.RfidViewModelFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// O enum e as funções de ajuda continuam os mesmos
private enum class StatusChecagem {
    AGUARDANDO_INPUT,
    ITEM_NAO_ENCONTRADO_NA_BASE,
    AGUARDANDO_LEITURA_FISICA,
    ITEM_ENCONTRADO,
    LOCALIZANDO
}

private fun normalizarRssi(rssi: String, minRssi: Int = -90, maxRssi: Int = -30): Float {
    return try {
        val rssiValue = rssi.toDouble().toInt()
        val normalized = (rssiValue - minRssi).toFloat() / (maxRssi - minRssi).toFloat()
        normalized.coerceIn(0f, 1f)
    } catch (e: Exception) {
        0f
    }
}

private var ItemInventario.rssi: Float by mutableStateOf(0f)


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TelaChecagem(
    onVoltar: () -> Unit,
    banco: AppDatabase
) {
    val context = LocalContext.current
    val viewModelFactory = RfidViewModelFactory(context)
    val viewModel: RfidViewModel = viewModel(factory = viewModelFactory)
    val scope = rememberCoroutineScope()

    var epcParaChecar by remember { mutableStateOf("") }
    var status by remember { mutableStateOf(StatusChecagem.AGUARDANDO_INPUT) }
    var itemDaBase by remember { mutableStateOf<ItemInventario?>(null) }
    val scanEvent by viewModel.scanEvent.collectAsState()

    // A lógica principal continua a mesma
    LaunchedEffect(status, scanEvent) {
        val evento = scanEvent
        if (evento != null && evento.epc == epcParaChecar) {
            if (status == StatusChecagem.AGUARDANDO_LEITURA_FISICA) {
                status = StatusChecagem.ITEM_ENCONTRADO
                viewModel.stopReading()
            }
            if (status == StatusChecagem.LOCALIZANDO) {
                itemDaBase?.let {
                    it.rssi = normalizarRssi(evento.rssi)
                }
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            viewModel.stopReading()
            viewModel.limparTags()
        }
    }

    Scaffold(
        topBar = {
            // O cabeçalho continua o mesmo
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .background(
                        Brush.verticalGradient(
                            0f to MaterialTheme.colorScheme.primaryContainer,
                            1f to MaterialTheme.colorScheme.primary
                        )
                    )
            ) {
                IconButton(onClick = onVoltar, modifier = Modifier.align(Alignment.CenterStart)) {
                    Icon(Icons.Default.ArrowBack, "Voltar", tint = Color.White)
                }
                Text("Checagem e Localização", style = MaterialTheme.typography.titleLarge, color = Color.White, modifier = Modifier.align(Alignment.Center))
            }
        }
    ) { paddingValues ->
        // ALTERAÇÃO: Layout unificado que se adapta ao estado, em vez de trocar a tela inteira.
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(Dimens.PaddingMedium),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // O campo de input some suavemente quando a busca começa.
            AnimatedVisibility(visible = status == StatusChecagem.AGUARDANDO_INPUT) {
                InputSection(
                    epcValue = epcParaChecar,
                    onEpcChange = { epcParaChecar = it }
                )
            }

            // O espaço do meio agora é a área de status, que se anima e se transforma.
            Spacer(Modifier.height(Dimens.PaddingLarge))
            StatusSection(
                status = status,
                item = itemDaBase,
                epcParaChecar = epcParaChecar
            )
            Spacer(Modifier.weight(1f))

            // A área de botões é fixa, mas os botões mudam conforme o estado.
            ActionsSection(
                status = status,
                epcParaChecar = epcParaChecar,
                onVerificarClick = {
                    if (epcParaChecar.isNotBlank()) {
                        scope.launch(Dispatchers.IO) {
                            val item = banco.inventarioDao().buscarPorTag(epcParaChecar)
                            withContext(Dispatchers.Main) {
                                itemDaBase = item
                                status = if (item != null) StatusChecagem.AGUARDANDO_LEITURA_FISICA else StatusChecagem.ITEM_NAO_ENCONTRADO_NA_BASE
                            }
                        }
                    }
                },
                onAtivarLocalizador = {
                    viewModel.limparTags()
                    viewModel.startReading()
                    status = StatusChecagem.LOCALIZANDO
                },
                onPararBusca = {
                    viewModel.stopReading()
                    status = StatusChecagem.AGUARDANDO_LEITURA_FISICA
                },
                onResetClick = {
                    epcParaChecar = ""
                    itemDaBase = null
                    status = StatusChecagem.AGUARDANDO_INPUT
                }
            )
        }
    }
}

@Composable
private fun InputSection(epcValue: String, onEpcChange: (String) -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(Dimens.PaddingSmall)) {
        Icon(Icons.Default.QrCodeScanner, "Scanner", modifier = Modifier.size(60.dp), tint = MaterialTheme.colorScheme.primary)
        Text("Digite a etiqueta (EPC) para checar na base.", textAlign = TextAlign.Center, style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(Dimens.PaddingSmall))
        StandardTextField(
            value = epcValue,
            onValueChange = onEpcChange,
            label = "Etiqueta (EPC)",
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun StatusSection(status: StatusChecagem, item: ItemInventario?, epcParaChecar: String) {
    // Anima a transição entre os diferentes conteúdos de status
    AnimatedContent(
        targetState = status,
        transitionSpec = { fadeIn(animationSpec = androidx.compose.animation.core.tween(300)) togetherWith fadeOut(animationSpec = androidx.compose.animation.core.tween(300)) },
        label = "statusAnimation"
    ) { targetStatus ->
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Dimens.PaddingMedium),
            modifier = Modifier.padding(Dimens.PaddingMedium)
        ) {
            when (targetStatus) {
                StatusChecagem.AGUARDANDO_INPUT -> {
                    // Estado inicial, não mostra nada aqui.
                }
                StatusChecagem.ITEM_NAO_ENCONTRADO_NA_BASE -> {
                    StatusIcon(icon = Icons.Default.ErrorOutline, color = MaterialTheme.colorScheme.error)
                    Text("Item Não Encontrado", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.error)
                    Text("Esta etiqueta não consta na base de dados.", color = RktTextSecondary, textAlign = TextAlign.Center)
                }
                StatusChecagem.AGUARDANDO_LEITURA_FISICA -> {
                    StatusIcon(icon = Icons.Default.HelpOutline, color = RktBlueInfo)
                    Text("Encontrado na Base", style = MaterialTheme.typography.titleLarge, color = RktBlueInfo)
                    Text("Pressione o gatilho para confirmar a presença.", color = RktTextSecondary, textAlign = TextAlign.Center)
                    DetalhesItemCard(item = item)
                }
                StatusChecagem.ITEM_ENCONTRADO -> {
                    StatusIcon(icon = Icons.Default.CheckCircle, color = RktGreen)
                    Text("Item Confirmado!", style = MaterialTheme.typography.titleLarge, color = RktGreen)
                    Text("A presença física do item foi validada.", color = RktTextSecondary, textAlign = TextAlign.Center)
                    DetalhesItemCard(item = item)
                }
                StatusChecagem.LOCALIZANDO -> {
                    Text("Localizando:", style = MaterialTheme.typography.titleMedium)
                    Text(epcParaChecar, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(Dimens.PaddingMedium))
                    SignalIndicator(progress = item?.rssi ?: 0f)
                }
            }
        }
    }
}

@Composable
private fun ActionsSection(
    status: StatusChecagem,
    epcParaChecar: String,
    onVerificarClick: () -> Unit,
    onAtivarLocalizador: () -> Unit,
    onPararBusca: () -> Unit,
    onResetClick: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        when(status) {
            StatusChecagem.AGUARDANDO_INPUT -> {
                PrimaryButton(onClick = onVerificarClick, text = "Verificar na Base", enabled = epcParaChecar.isNotBlank(), modifier = Modifier.fillMaxWidth())
            }
            StatusChecagem.ITEM_NAO_ENCONTRADO_NA_BASE -> {
                PrimaryButton(onClick = onResetClick, text = "Verificar Outra", modifier = Modifier.fillMaxWidth())
            }
            StatusChecagem.AGUARDANDO_LEITURA_FISICA -> {
                Text("Pressione o gatilho para ler ou...", style = MaterialTheme.typography.bodyMedium, color = RktTextSecondary)
                OutlinedButton(onClick = onAtivarLocalizador, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Default.TrackChanges, contentDescription = null, modifier = Modifier.size(ButtonDefaults.IconSize))
                    Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                    Text("Ativar Modo Localizador")
                }
            }
            StatusChecagem.ITEM_ENCONTRADO -> {
                PrimaryButton(onClick = onResetClick, text = "Verificar Outro Item", modifier = Modifier.fillMaxWidth())
            }
            StatusChecagem.LOCALIZANDO -> {
                PrimaryButton(onClick = onPararBusca, text = "Parar Busca", modifier = Modifier.fillMaxWidth())
            }
        }
    }
}

@Composable
private fun StatusIcon(icon: ImageVector, color: Color) {
    Icon(
        imageVector = icon,
        contentDescription = null,
        modifier = Modifier.size(80.dp),
        tint = color
    )
}

@Composable
private fun DetalhesItemCard(item: ItemInventario?) {
    if (item != null) {
        Card(
            modifier = Modifier.fillMaxWidth().padding(top = Dimens.PaddingSmall),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(
                modifier = Modifier.padding(Dimens.PaddingMedium),
                verticalArrangement = Arrangement.spacedBy(Dimens.PaddingSmall)
            ) {
                Text("Detalhes do Item:", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text("Descrição: ${item.desc.ifBlank { "N/A" }}")
                Text("Loja: ${item.loja.ifBlank { "N/A" }}")
                Text("Setor: ${item.localizacao.ifBlank { "N/A" }}")
            }
        }
    }
}

@Composable
private fun SignalIndicator(progress: Float) {
    val animatedProgress by animateFloatAsState(targetValue = progress, label = "signalAnimation")
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.size(180.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surfaceVariant)
    ) {
        CircularProgressIndicator(
            progress = { animatedProgress },
            modifier = Modifier.fillMaxSize(),
            strokeWidth = 12.dp,
            color = RktGreen,
            trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
        )
        Text(
            text = "${(animatedProgress * 100).toInt()}%",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
    }
}