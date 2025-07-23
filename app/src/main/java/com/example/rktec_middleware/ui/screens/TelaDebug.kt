@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.rktec_middleware.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.rktec_middleware.data.model.ItemInventario
import com.example.rktec_middleware.ui.components.GradientHeader
import com.example.rktec_middleware.ui.components.InfoChip
import com.example.rktec_middleware.ui.components.StandardTextField
import com.example.rktec_middleware.ui.theme.*
import com.example.rktec_middleware.viewmodel.DebugViewModel

@Composable
fun TelaDebug(
    usuarioLogado: String,
    onVoltar: () -> Unit,
    onBancoLimpo: () -> Unit,
    viewModel: DebugViewModel = hiltViewModel()
) {
    val inventarioCompleto by viewModel.inventarioCompleto.collectAsState()
    var mostrarDialogLimparBanco by remember { mutableStateOf(false) }
    var textoBusca by remember { mutableStateOf("") }
    var itemEditando by remember { mutableStateOf<ItemInventario?>(null) }
    val context = LocalContext.current

    val listasParaDropdown by remember(inventarioCompleto) {
        derivedStateOf {
            val lojas = inventarioCompleto.map { it.loja.replace("\"", "").trim() }.filter { it.isNotBlank() }.distinct().sorted()
            val setores = inventarioCompleto.map { it.localizacao.replace("\"", "").trim() }.filter { it.isNotBlank() }.distinct().sorted()
            Pair(lojas, setores)
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
                GradientHeader(title = "Consulta e Edição", onVoltar = onVoltar)
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
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
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    Column(Modifier.padding(Dimens.PaddingMedium)) {
                        Text(
                            "Total de itens: ${inventarioFiltrado.size} / ${inventarioCompleto.size}",
                            style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
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
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(Dimens.ComponentHeight),
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
                lojasDisponiveis = listasParaDropdown.first,
                setoresDisponiveis = listasParaDropdown.second,
                onDismiss = { itemEditando = null },
                onConfirm = { itemAtualizado ->
                    viewModel.atualizarItem(itemAtualizado)
                    Toast.makeText(context, "Item atualizado!", Toast.LENGTH_SHORT).show()
                    itemEditando = null
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
                            viewModel.limparBanco()
                            onBancoLimpo()
                            mostrarDialogLimparBanco = false
                            Toast.makeText(context, "Banco de dados limpo.", Toast.LENGTH_SHORT).show()
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
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
        elevation = CardDefaults.cardElevation(0.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
    ) {
        Column(Modifier.padding(Dimens.PaddingMedium)) {
            Text(text = item.desc.ifBlank { "Item sem descrição" }, style = MaterialTheme.typography.titleMedium)
            Text(text = item.tag, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Medium)
            Spacer(Modifier.height(Dimens.PaddingSmall))
            Row(horizontalArrangement = Arrangement.spacedBy(Dimens.PaddingSmall), verticalAlignment = Alignment.CenterVertically) {
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
private fun DialogEditarItemDebug(
    item: ItemInventario,
    lojasDisponiveis: List<String>,
    setoresDisponiveis: List<String>,
    onConfirm: (ItemInventario) -> Unit,
    onDismiss: () -> Unit
) {
    var desc by remember { mutableStateOf(item.desc) }
    var setor by remember { mutableStateOf(item.localizacao) }
    var loja by remember { mutableStateOf(item.loja) }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Default.Edit, contentDescription = "Editar Item") },
        title = { Text("Editar Item") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(Dimens.PaddingMedium)) {
                Text(
                    "EPC: ${item.tag}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
                Divider()
                StandardTextField(
                    value = desc,
                    onValueChange = { desc = it },
                    label = "Nome/Descrição"
                )

                // Dropdown para Lojas
                DropdownEditavel(
                    label = "Loja",
                    opcoes = lojasDisponiveis,
                    valorInicial = loja,
                    onValorSelecionado = { loja = it }
                )

                // Dropdown para Setores
                DropdownEditavel(
                    label = "Setor",
                    opcoes = setoresDisponiveis,
                    valorInicial = setor,
                    onValorSelecionado = { setor = it }
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                onConfirm(item.copy(desc = desc, localizacao = setor, loja = loja))
            }) { Text("Salvar") }
        },
        dismissButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("Cancelar")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DropdownEditavel(
    label: String,
    opcoes: List<String>,
    valorInicial: String,
    onValorSelecionado: (String) -> Unit
) {
    var expandido by remember { mutableStateOf(false) }
    var valorSelecionado by remember { mutableStateOf(valorInicial) }

    ExposedDropdownMenuBox(
        expanded = expandido,
        onExpandedChange = { expandido = !expandido },
        modifier = Modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = valorSelecionado,
            onValueChange = { },
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandido) },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.Black,
                unfocusedTextColor = Color.Black,
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
            )
        )
        ExposedDropdownMenu(
            expanded = expandido,
            onDismissRequest = { expandido = false },
            modifier = Modifier
                .background(Color.White)
                .heightIn(max = 220.dp)
        ) {
            opcoes.forEach { opcao ->
                DropdownMenuItem(
                    text = { Text(opcao, color = Color.Black) },
                    onClick = {
                        valorSelecionado = opcao
                        onValorSelecionado(opcao)
                        expandido = false
                    }
                )
            }
        }
    }
}
