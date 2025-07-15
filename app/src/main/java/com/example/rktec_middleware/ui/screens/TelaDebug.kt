@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.rktec_middleware.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.rktec_middleware.data.db.AppDatabase
import com.example.rktec_middleware.data.model.*
import com.example.rktec_middleware.ui.components.InfoChip
import com.example.rktec_middleware.ui.components.StandardTextField
import com.example.rktec_middleware.ui.theme.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun TelaDebug(
    banco: AppDatabase,
    usuarioLogado: String,
    refresh: Int,
    onVoltar: () -> Unit,
    onBancoLimpo: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var inventarioCompleto by remember { mutableStateOf<List<ItemInventario>>(emptyList()) }
    var mostrarDialogLimparBanco by remember { mutableStateOf(false) }
    var textoBusca by remember { mutableStateOf("") }
    var itemEditando by remember { mutableStateOf<ItemInventario?>(null) }
    val context = LocalContext.current

    LaunchedEffect(refresh) {
        inventarioCompleto = withContext(Dispatchers.IO) {
            banco.inventarioDao().listarTodos()
        }
    }

    val inventarioFiltrado by remember(textoBusca, inventarioCompleto) {
        derivedStateOf {
            if (textoBusca.isBlank()) inventarioCompleto
            else inventarioCompleto.filter {
                it.tag.contains(textoBusca, true) || it.desc.contains(textoBusca, true) ||
                        it.localizacao.contains(textoBusca, true) || it.loja.contains(textoBusca, true)
            }
        }
    }

    RKTecMiddlewareTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Consulta e Edição") },
                    navigationIcon = { IconButton(onClick = onVoltar) { Icon(Icons.Default.ArrowBack, "Voltar") } },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier.padding(paddingValues).fillMaxSize().background(MaterialTheme.colorScheme.background)
                    .padding(Dimens.PaddingMedium),
                verticalArrangement = Arrangement.spacedBy(Dimens.PaddingMedium)
            ) {
                StandardTextField(
                    value = textoBusca,
                    onValueChange = { textoBusca = it },
                    label = "Buscar por EPC, nome, loja ou setor",
                    leadingIcon = { Icon(Icons.Default.Search, "Buscar") }
                )

                Card(
                    shape = MaterialTheme.shapes.medium,
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth().weight(1f)
                ) {
                    Column(Modifier.padding(Dimens.PaddingMedium)) {
                        Text(
                            "Total de itens: ${inventarioFiltrado.size} / ${inventarioCompleto.size}",
                            style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primaryContainer
                        )
                        Spacer(Modifier.height(Dimens.PaddingSmall))
                        if (inventarioCompleto.isEmpty()) {
                            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text("Nenhum item de inventário importado.", color = RktTextSecondary)
                            }
                        } else {
                            LazyColumn(verticalArrangement = Arrangement.spacedBy(Dimens.PaddingSmall)) {
                                items(inventarioFiltrado, key = { it.tag }) { item ->
                                    ItemDebugCard(item) { itemEditando = item }
                                }
                            }
                        }
                    }
                }
                Button(
                    onClick = { mostrarDialogLimparBanco = true },
                    modifier = Modifier.fillMaxWidth().height(Dimens.ComponentHeight),
                    shape = MaterialTheme.shapes.medium,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Limpar Banco de Dados", style = MaterialTheme.typography.labelLarge)
                }
            }
        }

        if (itemEditando != null) {
            DialogEditarItemDebug(
                item = itemEditando!!,
                onDismiss = { itemEditando = null },
                onConfirm = { itemAtualizado ->
                    scope.launch {
                        withContext(Dispatchers.IO) {
                            banco.inventarioDao().atualizarItem(itemAtualizado)
                        }
                        val index = inventarioCompleto.indexOfFirst { it.tag == itemAtualizado.tag }
                        if (index != -1) {
                            val novaLista = inventarioCompleto.toMutableList()
                            novaLista[index] = itemAtualizado
                            inventarioCompleto = novaLista
                        }
                        Toast.makeText(context, "Item atualizado!", Toast.LENGTH_SHORT).show()
                        itemEditando = null
                    }
                }
            )
        }

        if (mostrarDialogLimparBanco) {
            AlertDialog(
                onDismissRequest = { mostrarDialogLimparBanco = false },
                icon = { Icon(Icons.Default.Warning, "Atenção", tint = RktRed) },
                title = { Text("Limpar TODOS os dados?") },
                text = { Text("Isso vai apagar todo o inventário, coletas, logs e mapeamentos. A ação não pode ser desfeita.") },
                confirmButton = {
                    Button(
                        onClick = {
                            mostrarDialogLimparBanco = false
                            scope.launch {
                                // CORREÇÃO APLICADA AQUI
                                withContext(Dispatchers.IO) {
                                    banco.clearAllTables() // Executando em segundo plano
                                }
                                onBancoLimpo()
                                Toast.makeText(context, "Banco de dados limpo.", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = RktRed)
                    ) { Text("APAGAR TUDO") }
                },
                dismissButton = { OutlinedButton(onClick = { mostrarDialogLimparBanco = false }) { Text("Cancelar") } }
            )
        }
    }
}
@Composable
private fun ItemDebugCard(item: ItemInventario, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(Modifier.padding(Dimens.PaddingMedium)) {
            Text(text = item.desc.ifBlank { "Item sem descrição" }, style = MaterialTheme.typography.titleLarge)
            Text(text = item.tag, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Medium)
            Spacer(Modifier.height(Dimens.PaddingSmall))
            Row(horizontalArrangement = Arrangement.spacedBy(Dimens.PaddingSmall)) {
                if (item.loja.isNotBlank()) InfoChip("Loja: ${item.loja}")
                if (item.localizacao.isNotBlank()) InfoChip("Setor: ${item.localizacao}")
            }
            AnimatedVisibility(item.colunasExtras.isNotEmpty()) {
                Column {
                    Divider(modifier = Modifier.padding(vertical = Dimens.PaddingSmall))
                    Text("Dados adicionais:", style = MaterialTheme.typography.labelMedium, color = RktTextSecondary)
                    item.colunasExtras.forEach { (chave, valor) ->
                        Text("• $chave: $valor", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    }
}


@Composable
private fun DialogEditarItemDebug(item: ItemInventario, onConfirm: (ItemInventario) -> Unit, onDismiss: () -> Unit) {
    var desc by remember { mutableStateOf(item.desc) }
    var setor by remember { mutableStateOf(item.localizacao) }
    var loja by remember { mutableStateOf(item.loja) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Editar Item") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(Dimens.PaddingSmall)) {
                Text("EPC: ${item.tag}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                StandardTextField(value = desc, onValueChange = { desc = it }, label = "Nome/Descrição")
                StandardTextField(value = setor, onValueChange = { setor = it }, label = "Setor")
                StandardTextField(value = loja, onValueChange = { loja = it }, label = "Loja")
            }
        },
        confirmButton = {
            Button(onClick = {
                onConfirm(item.copy(desc = desc, localizacao = setor, loja = loja))
            }) { Text("Salvar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}