package com.example.rktec_middleware.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.rktec_middleware.data.model.LogGerenciamentoUsuario
import com.example.rktec_middleware.ui.components.GradientHeader
import com.example.rktec_middleware.ui.theme.Dimens
import com.example.rktec_middleware.ui.theme.RktTextSecondary
import com.example.rktec_middleware.viewmodel.LogViewModel
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TelaLogAtividades(
    onVoltar: () -> Unit,
    viewModel: LogViewModel = hiltViewModel()
) {
    val logs by viewModel.logs.collectAsState()

    Scaffold(
        topBar = {
            GradientHeader(
                title = "Log de Atividades",
                subtitle = "Ações administrativas registradas",
                onVoltar = onVoltar
            )
        }
    ) { paddingValues ->
        if (logs.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                Text("Nenhum registro de atividade encontrado.")
            }
        } else {
            LazyColumn(
                modifier = Modifier.padding(paddingValues).fillMaxSize(),
                contentPadding = PaddingValues(Dimens.PaddingMedium),
                verticalArrangement = Arrangement.spacedBy(Dimens.PaddingMedium)
            ) {
                items(logs, key = { it.id }) { log ->
                    LogCardExpansivel(log = log)
                }
            }
        }
    }
}

@Composable
private fun LogCardExpansivel(log: LogGerenciamentoUsuario) {
    var expandido by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth().clickable { expandido = !expandido },
        elevation = CardDefaults.cardElevation(Dimens.PaddingExtraSmall),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(Modifier.padding(Dimens.PaddingMedium)) {
            // Cabeçalho sempre visível
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = when (log.acao) {
                        "EDIÇÃO DE ITEM" -> Icons.Default.EditNote
                        "LIMPEZA DE DADOS" -> Icons.Default.DeleteForever
                        else -> Icons.Default.ManageAccounts
                    },
                    contentDescription = "Log",
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.width(Dimens.PaddingSmall))
                Text(
                    text = log.acao,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.weight(1f))
                Icon(
                    imageVector = if (expandido) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = "Expandir"
                )
            }

            // Conteúdo expansível
            AnimatedVisibility(visible = expandido) {
                Column {
                    Divider(Modifier.padding(vertical = Dimens.PaddingSmall))
                    InfoLog(label = "Responsável:", value = log.usuarioResponsavel)
                    InfoLog(label = "Data/Hora:", value = log.dataHora)
                    InfoLog(label = "Alvo:", value = log.usuarioAlvo)
                    if (!log.motivo.isNullOrBlank()) {
                        InfoLog(label = "Motivo:", value = log.motivo)
                    }
                    InfoLog(label = "Detalhes:", value = log.detalhes)
                }
            }
        }
    }
}

@Composable
private fun LogCard(log: LogGerenciamentoUsuario) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(Dimens.PaddingExtraSmall),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(Modifier.padding(Dimens.PaddingMedium)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Assignment,
                    contentDescription = "Log",
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.width(Dimens.PaddingSmall))
                Text(
                    text = log.acao,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.weight(1f))
                Text(
                    text = log.dataHora,
                    style = MaterialTheme.typography.bodySmall,
                    color = RktTextSecondary
                )
            }
            Divider(Modifier.padding(vertical = Dimens.PaddingSmall))
            InfoLog(label = "Responsável:", value = log.usuarioResponsavel)
            InfoLog(label = "Alvo:", value = log.usuarioAlvo)
            if (!log.motivo.isNullOrBlank()) {
                InfoLog(label = "Motivo:", value = log.motivo)
            }
            InfoLog(label = "Detalhes:", value = log.detalhes)
        }
    }
}

@Composable
private fun InfoLog(label: String, value: String) {
    Row {
        Text(
            text = label,
            fontWeight = FontWeight.SemiBold,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.width(100.dp) // Alinha os valores
        )
        Text(text = value, style = MaterialTheme.typography.bodyMedium)
    }
}