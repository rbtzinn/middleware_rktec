// ui/screens/TelaLeituraColeta.kt
@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.rktec_middleware.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Nfc
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
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
    var naoMostrarLimpar by rememberSaveable { mutableStateOf(false) }

    RKTecMiddlewareTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Coleta de Tags") },
                    navigationIcon = {
                        IconButton(onClick = { isDialogVoltarAberto = true }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Voltar")
                        }
                    },
                    actions = {
                        IconButton(onClick = {
                            if (naoMostrarLimpar || tagList.isEmpty()) {
                                viewModel.limparTags()
                            } else {
                                isDialogLimparAberto = true
                            }
                        }) {
                            Icon(Icons.Default.Delete, contentDescription = "Limpar tags")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        actionIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
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
                Text("Total de tags lidas: ${tagList.size}", style = MaterialTheme.typography.bodyLarge)
                Spacer(Modifier.height(Dimens.PaddingMedium))

                Card(
                    modifier = Modifier.fillMaxWidth().weight(1f).padding(horizontal = Dimens.PaddingMedium),
                    shape = MaterialTheme.shapes.medium,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(Dimens.PaddingExtraSmall)
                ) {
                    if (tagList.isEmpty()) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("Nenhuma etiqueta lida ainda", color = RktTextSecondary)
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
                    onClick = onVoltar, // A lógica de limpeza já está no dialog de "voltar"
                    text = "Finalizar Coleta",
                    modifier = Modifier.padding(horizontal = Dimens.PaddingMedium)
                )
            }
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
                    Row(verticalAlignment = Alignment.CenterVertically) {
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
}

@Composable
private fun ColetaTagItem(epc: String) {
    Card(
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = RktBlueLight),
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
                color = RktTextPrimary,
                fontWeight = FontWeight.Medium,
                maxLines = 1
            )
        }
    }
}