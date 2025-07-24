@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.rktec_middleware.ui.screens

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.rktec_middleware.data.model.ItemSessao
import com.example.rktec_middleware.data.model.StatusItemSessao
import com.example.rktec_middleware.ui.components.GradientHeader
import com.example.rktec_middleware.ui.theme.*
import com.example.rktec_middleware.viewmodel.DetalheHistoricoViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun TelaDetalheHistorico(
    onVoltar: () -> Unit,
    viewModel: DetalheHistoricoViewModel = hiltViewModel()
) {
    val sessao by viewModel.sessao.collectAsState()
    val itens by viewModel.itensDaSessao.collectAsState()

    Scaffold(
        topBar = {
            GradientHeader(title = "Detalhes do Inventário", onVoltar = onVoltar)
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier.padding(paddingValues).fillMaxSize(),
            contentPadding = PaddingValues(Dimens.PaddingMedium),
            verticalArrangement = Arrangement.spacedBy(Dimens.PaddingSmall)
        ) {
            item {
                sessao?.let { DetalheHeader(it) }
            }
            items(itens, key = { it.id }) { item ->
                ItemDetalheCard(item)
            }
        }
    }
}

@Composable
private fun DetalheHeader(sessao: com.example.rktec_middleware.data.model.SessaoInventario) {
    val formatador = remember { SimpleDateFormat("dd/MM/yyyy 'às' HH:mm", Locale.getDefault()) }
    Card(
        modifier = Modifier.fillMaxWidth().padding(bottom = Dimens.PaddingSmall),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(Modifier.padding(Dimens.PaddingMedium)) {
            Text("Relatório de ${formatador.format(Date(sessao.dataHora))}", style = MaterialTheme.typography.titleLarge)
            Text("Loja: ${sessao.filtroLoja ?: "Todas"} | Setor: ${sessao.filtroSetor ?: "Todos"}")
            Text("Responsável: ${sessao.usuarioResponsavel}", fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun ItemDetalheCard(item: ItemSessao) {
    val statusColor = when(item.status) {
        StatusItemSessao.ENCONTRADO -> RktGreen
        StatusItemSessao.FALTANTE -> RktRed
        else -> RktOrange
    }
    val statusIcon = when(item.status) {
        StatusItemSessao.ENCONTRADO -> Icons.Default.CheckCircle
        StatusItemSessao.FALTANTE -> Icons.Default.Error
        else -> Icons.Default.Warning
    }

    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(Dimens.PaddingSmall)) {
        Icon(statusIcon, contentDescription = null, tint = statusColor)
        Spacer(Modifier.width(Dimens.PaddingMedium))
        Column(Modifier.weight(1f)) {
            Text(item.epc, fontWeight = FontWeight.Bold)
            Text(item.descricao, style = MaterialTheme.typography.bodySmall)
        }
        Text(item.status.name.replace("_", " "), style = MaterialTheme.typography.bodySmall, color = statusColor)
    }
}