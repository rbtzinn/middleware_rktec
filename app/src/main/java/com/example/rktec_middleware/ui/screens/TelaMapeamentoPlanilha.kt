package com.example.rktec_middleware.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.rktec_middleware.ui.components.PrimaryButton
import com.example.rktec_middleware.ui.components.SecondaryTextButton
import com.example.rktec_middleware.ui.theme.*
import com.example.rktec_middleware.util.LeitorInventario
import com.example.rktec_middleware.viewmodel.MapeamentoState
import com.example.rktec_middleware.viewmodel.MapeamentoViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TelaMapeamentoPlanilha(
    uri: Uri,
    usuarioLogado: String,
    onSalvar: (totalItens: Int) -> Unit,
    onCancelar: () -> Unit,
    viewModel: MapeamentoViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    var dadosBrutos by remember { mutableStateOf<Pair<List<String>, List<List<String>>>?>(null) }
    var indexEpc by remember { mutableStateOf<Int?>(null) }
    var indexNome by remember { mutableStateOf<Int?>(null) }
    var indexSetor by remember { mutableStateOf<Int?>(null) }
    var indexLoja by remember { mutableStateOf<Int?>(null) }
    var isReadingFile by remember { mutableStateOf(true) }

    val mapeamentoState by viewModel.mapeamentoState.collectAsState()

    LaunchedEffect(uri) {
        withContext(Dispatchers.IO) {
            dadosBrutos = LeitorInventario.lerDadosBrutosDaPlanilha(context, uri)
        }
        isReadingFile = false
        if (dadosBrutos == null) {
            Toast.makeText(context, "Erro ao ler o arquivo ou formato não suportado.", Toast.LENGTH_LONG).show()
            onCancelar()
        }
    }

    LaunchedEffect(mapeamentoState) {
        when(val state = mapeamentoState) {
            is MapeamentoState.Success -> {
                Toast.makeText(context, "${state.totalItens} itens importados com sucesso!", Toast.LENGTH_SHORT).show()
                onSalvar(state.totalItens)
            }
            is MapeamentoState.Error -> {
                Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
            }
            else -> {}
        }
    }

    val colunas = dadosBrutos?.first ?: emptyList()
    val isLoading = isReadingFile || mapeamentoState is MapeamentoState.Loading

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mapeamento de Planilha") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { innerPadding ->
        if (isReadingFile) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
                Text("Lendo arquivo...", modifier = Modifier.padding(top = 80.dp))
            }
        } else {
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .background(MaterialTheme.colorScheme.background)
            ) {
                Column(
                    modifier = Modifier.padding(Dimens.PaddingMedium),
                    verticalArrangement = Arrangement.spacedBy(Dimens.PaddingMedium)
                ) {
                    InfoCard("Selecione a correspondência entre as colunas do arquivo e os campos do sistema.")

                    Card(
                        shape = MaterialTheme.shapes.large,
                        elevation = CardDefaults.cardElevation(Dimens.PaddingExtraSmall),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(Modifier.padding(Dimens.PaddingLarge), verticalArrangement = Arrangement.spacedBy(Dimens.PaddingLarge)) {
                            CampoMapeamento(titulo = "Coluna do EPC*", descricao = "Identificador único do ativo.", colunas = colunas, selecionado = indexEpc, onSelecionado = { indexEpc = it })
                            CampoMapeamento(titulo = "Coluna do Nome/Descrição", descricao = "Nome descritivo do ativo.", colunas = colunas, selecionado = indexNome, onSelecionado = { indexNome = it })
                            CampoMapeamento(titulo = "Coluna do Setor", descricao = "Localização física do ativo.", colunas = colunas, selecionado = indexSetor, onSelecionado = { indexSetor = it })
                            CampoMapeamento(titulo = "Coluna da Loja", descricao = "Unidade empresarial do ativo.", colunas = colunas, selecionado = indexLoja, onSelecionado = { indexLoja = it })
                        }
                    }
                }

                Spacer(Modifier.weight(1f))

                Column(Modifier.padding(Dimens.PaddingMedium), horizontalAlignment = Alignment.CenterHorizontally) {
                    PrimaryButton(
                        onClick = {
                            viewModel.processarEsalvarDados(
                                usuario = usuarioLogado,
                                uri = uri,
                                dadosBrutos = dadosBrutos,
                                indexEpc = indexEpc,
                                indexNome = indexNome,
                                indexSetor = indexSetor,
                                indexLoja = indexLoja
                            )
                        },
                        text = if (isLoading) "Processando..." else "Confirmar e Importar",
                        enabled = indexEpc != null && !isLoading
                    )
                    SecondaryTextButton(onClick = onCancelar, text = "Cancelar")
                }
            }
        }
    }
}

@Composable
private fun InfoCard(text: String) {
    Card(shape = MaterialTheme.shapes.medium, colors = CardDefaults.cardColors(containerColor = RktBlueLight)) {
        Row(Modifier.padding(Dimens.PaddingMedium), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Info, contentDescription = "Informação", tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.width(Dimens.PaddingSmall))
            Text(text = text, style = MaterialTheme.typography.bodyLarge, color = RktTextPrimary)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CampoMapeamento(
    titulo: String,
    descricao: String,
    colunas: List<String>,
    selecionado: Int?,
    onSelecionado: (Int?) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Column {
        Text(text = titulo, style = MaterialTheme.typography.titleLarge)
        Text(text = descricao, style = MaterialTheme.typography.bodyMedium)
        Spacer(modifier = Modifier.height(Dimens.PaddingSmall))
        ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
            OutlinedTextField(
                value = selecionado?.let { colunas.getOrNull(it) } ?: "Nenhuma seleção", // <-- TEXTO CORRIGIDO
                onValueChange = {},
                readOnly = true,
                modifier = Modifier.menuAnchor().fillMaxWidth(),
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                shape = MaterialTheme.shapes.medium,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = RktStroke
                )
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.background(MaterialTheme.colorScheme.surface)
            ) {
                DropdownMenuItem(
                    text = { Text("Nenhuma seleção", color = MaterialTheme.colorScheme.onSurface) },
                    onClick = {
                        onSelecionado(null)
                        expanded = false
                    }
                )
                Divider()
                colunas.forEachIndexed { idx, col ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                col,
                                color = MaterialTheme.colorScheme.onSurface,
                                // ESTILO CORRIGIDO: Só fica em negrito se for a seleção atual
                                fontWeight = if (selecionado == idx) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        onClick = {
                            onSelecionado(idx)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}