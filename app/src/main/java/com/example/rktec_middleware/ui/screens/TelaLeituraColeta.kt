@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.rktec_middleware.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Nfc
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import com.example.rktec_middleware.ui.components.GradientHeader
import com.example.rktec_middleware.ui.components.PrimaryButton
import com.example.rktec_middleware.ui.theme.*
import com.example.rktec_middleware.viewmodel.RfidViewModel

@Composable
fun TelaLeituraColeta(
    viewModel: RfidViewModel,
    onVoltar: () -> Unit
) {
    val tagList by viewModel.tagList.collectAsState()
    var isDialogVoltarAberto by remember { mutableStateOf(false) }
    var isDialogLimparAberto by remember { mutableStateOf(false) }
    var naoMostrarLimpar by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            GradientHeader(
                title = "Coleta de Tags",
                onVoltar = { isDialogVoltarAberto = true }
            ) {
                IconButton(onClick = {
                    if (naoMostrarLimpar || tagList.isEmpty()) {
                        viewModel.limparTags()
                    } else {
                        isDialogLimparAberto = true
                    }
                }) {
                    Icon(Icons.Default.Delete, contentDescription = "Limpar tags", tint = Color.White)
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(bottom = Dimens.PaddingMedium),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(Dimens.PaddingMedium))
            Text("Pressione o gatilho para ler", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
            Text("Total de tags únicas lidas: ${tagList.size}", style = MaterialTheme.typography.bodyLarge)
            Spacer(Modifier.height(Dimens.PaddingMedium))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = Dimens.PaddingMedium),
                shape = MaterialTheme.shapes.medium,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(Dimens.PaddingExtraSmall)
            ) {
                if (tagList.isEmpty()) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Nenhuma etiqueta lida ainda")
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(Dimens.PaddingSmall),
                        verticalArrangement = Arrangement.spacedBy(Dimens.PaddingSmall)
                    ) {
                        items(tagList, key = { it.epc }) { tag ->
                            ColetaTagItem(epc = tag.epc)
                        }
                    }
                }
            }
            Spacer(Modifier.height(Dimens.PaddingMedium))
            PrimaryButton(
                // AJUSTE: O botão de finalizar agora também abre o diálogo de confirmação
                onClick = { isDialogVoltarAberto = true },
                text = "Finalizar Coleta",
                modifier = Modifier.padding(horizontal = Dimens.PaddingMedium)
            )
        }
    }

    if (isDialogVoltarAberto) {
        AlertDialog(
            onDismissRequest = { isDialogVoltarAberto = false },
            title = { Text("Confirmar saída") },
            text = { Text("Deseja realmente voltar? As tags lidas serão apagadas.") },
            confirmButton = {
                Button(onClick = {
                    viewModel.limparTags()
                    isDialogVoltarAberto = false
                    onVoltar()
                }) { Text("Sim, Sair") }
            },
            dismissButton = { TextButton(onClick = { isDialogVoltarAberto = false }) { Text("Não") } },
            shape = MaterialTheme.shapes.large
        )
    }

    if (isDialogLimparAberto) {
        AlertDialog(
            onDismissRequest = { isDialogLimparAberto = false },
            title = { Text("Confirmar limpeza") },
            text = {
                Column {
                    Text("Deseja realmente limpar as tags lidas?")
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { naoMostrarLimpar = !naoMostrarLimpar }) {
                        Checkbox(checked = naoMostrarLimpar, onCheckedChange = { naoMostrarLimpar = it })
                        Text("Não mostrar esta mensagem novamente")
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    viewModel.limparTags()
                    isDialogLimparAberto = false
                }) { Text("Limpar") }
            },
            dismissButton = { TextButton(onClick = { isDialogLimparAberto = false }) { Text("Cancelar") } },
            shape = MaterialTheme.shapes.large
        )
    }
} // <-- CORREÇÃO: A chave de fechamento da função foi movida para aqui, englobando os diálogos.

@Composable
private fun ColetaTagItem(epc: String) {
    Card(
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = Dimens.PaddingMedium, vertical = Dimens.PaddingSmall)
        ) {
            Icon(
                imageVector = Icons.Default.Nfc,
                contentDescription = "Tag",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(Dimens.IconSizeSmall)
            )
            Spacer(modifier = Modifier.width(Dimens.PaddingSmall))
            Text(
                text = epc,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                maxLines = 1
            )
        }
    }
}