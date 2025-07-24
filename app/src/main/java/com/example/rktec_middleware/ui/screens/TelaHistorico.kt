@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.rktec_middleware.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.rktec_middleware.data.model.SessaoInventario
import com.example.rktec_middleware.ui.components.GradientHeader
import com.example.rktec_middleware.ui.theme.*
import com.example.rktec_middleware.viewmodel.HistoricoViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun TelaHistorico(
    onVoltar: () -> Unit,
    onSessaoClick: (Long) -> Unit,
    viewModel: HistoricoViewModel = hiltViewModel()
) {
    val sessoes by viewModel.sessoes.collectAsState()

    Scaffold(
        topBar = {
            GradientHeader(title = "Histórico de Inventários", onVoltar = onVoltar)
        }
    ) { paddingValues ->
        if (sessoes.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                Text("Nenhum histórico encontrado.", style = MaterialTheme.typography.bodyLarge)
            }
        } else {
            LazyColumn(
                modifier = Modifier.padding(paddingValues).fillMaxSize(),
                contentPadding = PaddingValues(Dimens.PaddingMedium),
                verticalArrangement = Arrangement.spacedBy(Dimens.PaddingMedium)
            ) {
                items(sessoes, key = { it.id }) { sessao ->
                    SessaoCard(sessao = sessao, onClick = { onSessaoClick(sessao.id) })
                }
            }
        }
    }
}

@Composable
private fun SessaoCard(sessao: SessaoInventario, onClick: () -> Unit) {
    val formatadorData = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }
    val formatadorHora = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }

    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(Dimens.PaddingExtraSmall),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(Modifier.padding(Dimens.PaddingMedium)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(
                        text = "Loja: ${sessao.filtroLoja ?: "Todas"} | Setor: ${sessao.filtroSetor ?: "Todos"}",
                        style = MaterialTheme.typography.titleLarge
                    )
                    Text(
                        text = "Por ${sessao.usuarioResponsavel}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(formatadorData.format(Date(sessao.dataHora)), style = MaterialTheme.typography.bodyMedium)
                    Text(formatadorHora.format(Date(sessao.dataHora)), style = MaterialTheme.typography.bodyMedium)
                }
            }
            Divider(Modifier.padding(vertical = Dimens.PaddingSmall))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                InfoInventario("Esperado", sessao.totalEsperado.toString(), RktTextSecondary)
                InfoInventario("Encontrado", sessao.totalEncontrado.toString(), RktGreen)
                InfoInventario("Faltante", sessao.totalFaltante.toString(), RktRed)
                InfoInventario("Adicional", sessao.totalAdicional.toString(), RktOrange)
            }
        }
    }
}

@Composable
private fun InfoInventario(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, style = MaterialTheme.typography.labelMedium)
        Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = color)
    }
}