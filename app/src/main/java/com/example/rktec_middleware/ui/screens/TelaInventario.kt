package com.example.rktec_middleware.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.rktec_middleware.data.model.ItemInventario
import com.example.rktec_middleware.ui.components.InfoChip
import com.example.rktec_middleware.ui.components.PrimaryButton
import com.example.rktec_middleware.ui.components.StandardDropdown
import com.example.rktec_middleware.ui.theme.*
import com.example.rktec_middleware.viewmodel.InventarioViewModel

private fun normalizarNome(nome: String): String {
    return nome
        .replace("\"", "")
        .trim()
        .uppercase()
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TelaInventario(
    onVoltar: () -> Unit,
    onIniciarLeituraInventario: (
        filtroLoja: String?,
        filtroSetor: String?
    ) -> Unit,
    onSobreClick: () -> Unit,
    viewModel: InventarioViewModel = hiltViewModel()
) {
    val dadosImportados by viewModel.dadosImportados.collectAsState()
    var filtroLoja by remember { mutableStateOf<String?>(null) }
    var filtroSetor by remember { mutableStateOf<String?>(null) }

    val lojasDisponiveis by remember(dadosImportados) {
        derivedStateOf {
            dadosImportados.map { normalizarNome(it.loja) }.filter { it.isNotBlank() }.distinct()
        }
    }

    val setoresDisponiveis by remember(filtroLoja, dadosImportados) {
        derivedStateOf {
            dadosImportados
                .filter { item -> filtroLoja.isNullOrBlank() || normalizarNome(item.loja) == filtroLoja }
                .map { it.localizacao.trim() }.filter { it.isNotBlank() }.distinct()
        }
    }

    val listaFiltrada = dadosImportados.filter { item ->
        (filtroLoja.isNullOrEmpty() || normalizarNome(item.loja) == filtroLoja) &&
                (filtroSetor.isNullOrEmpty() || item.localizacao.trim() == filtroSetor)
    }

        Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .background(
                        Brush.verticalGradient(
                            0f to MaterialTheme.colorScheme.primaryContainer,
                            1f to MaterialTheme.colorScheme.primary
                        )
                    )
            ) {
                IconButton(
                    onClick = onVoltar,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(Dimens.PaddingSmall)
                ) {
                    Icon(
                        Icons.Default.ArrowBack,
                        contentDescription = "Voltar",
                        tint = Color.White,
                        modifier = Modifier.size(Dimens.IconSizeLarge)
                    )
                }
                Text(
                    "Inventário",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White,
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            Column(
                Modifier
                    .fillMaxWidth()
                    .offset(y = (-25).dp)
                    .padding(horizontal = Dimens.PaddingLarge),
                verticalArrangement = Arrangement.spacedBy(Dimens.PaddingMedium)
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.large,
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(Modifier.padding(Dimens.PaddingMedium)) {
                        Text(
                            "Filtros rápidos",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.primaryContainer
                        )
                        Spacer(Modifier.height(Dimens.PaddingSmall))
                        Row(horizontalArrangement = Arrangement.spacedBy(Dimens.PaddingSmall)) {
                            StandardDropdown(
                                label = "Loja",
                                options = listOf("Todos") + lojasDisponiveis,
                                selectedOption = filtroLoja ?: "Todos",
                                onOptionSelected = {
                                    filtroLoja = if (it == "Todos") null else it
                                    filtroSetor = null
                                },
                                modifier = Modifier.weight(1f)
                            )
                            StandardDropdown(
                                label = "Setor",
                                options = listOf("Todos") + setoresDisponiveis,
                                selectedOption = filtroSetor ?: "Todos",
                                onOptionSelected = { filtroSetor = if (it == "Todos") null else it },
                                modifier = Modifier.weight(1f),
                                enabled = !filtroLoja.isNullOrBlank()
                            )
                        }
                        Text(
                            "Itens: ${listaFiltrada.size} / ${dadosImportados.size}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .align(Alignment.End)
                                .padding(top = Dimens.PaddingSmall)
                        )
                    }
                }
            }

            if (dadosImportados.isEmpty()) {
                Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    Text(
                        "Nenhum item de inventário encontrado",
                        style = MaterialTheme.typography.bodyLarge,
                        color = RktTextSecondary
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(
                        start = Dimens.PaddingMedium,
                        end = Dimens.PaddingMedium,
                        bottom = Dimens.PaddingMedium
                    ),
                    verticalArrangement = Arrangement.spacedBy(Dimens.PaddingSmall)
                ) {
                    items(listaFiltrada, key = { it.tag }) { item ->
                        ItemInventarioCard(item)
                    }
                }
            }

            if (dadosImportados.isNotEmpty()) {
                PrimaryButton(
                    onClick = {
                        onIniciarLeituraInventario(
                            filtroLoja,
                            filtroSetor,
                        )
                    },
                    text = "Iniciar Leitura",
                    modifier = Modifier.padding(Dimens.PaddingMedium)
                )
            }

            Text(
                "RKTECNOLOGIAS",
                style = MaterialTheme.typography.labelMedium,
                color = RktGreen,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = Dimens.PaddingSmall)
                    .clickable(onClick = onSobreClick),
                textAlign = TextAlign.Center
            )
        }
    }

// A função ItemInventarioCard permanece a mesma.
@Composable
private fun ItemInventarioCard(item: ItemInventario) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(Modifier.padding(Dimens.PaddingMedium)) {
            Text(
                text = item.desc.ifBlank { "Sem descrição" },
                style = MaterialTheme.typography.titleLarge,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = item.tag,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Row(
                Modifier.padding(top = Dimens.PaddingSmall),
                horizontalArrangement = Arrangement.spacedBy(Dimens.PaddingSmall)
            ) {
                if (item.loja.isNotBlank()) InfoChip("Loja: ${normalizarNome(item.loja)}")
                if (item.localizacao.isNotBlank()) InfoChip("Setor: ${item.localizacao}")
            }
        }
    }
}