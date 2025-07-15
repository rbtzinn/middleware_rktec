package com.example.rktec_middleware.ui.screens

import android.content.Context
import android.net.Uri
import android.util.Log
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
import com.example.rktec_middleware.data.db.AppDatabase
import com.example.rktec_middleware.data.model.ItemInventario
import com.example.rktec_middleware.data.model.MapeamentoPlanilha
import com.example.rktec_middleware.ui.components.PrimaryButton
import com.example.rktec_middleware.ui.components.SecondaryTextButton
import com.example.rktec_middleware.ui.theme.*
import com.example.rktec_middleware.util.LeitorInventario
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TelaMapeamentoPlanilha(
    uri: Uri,
    usuarioLogado: String,
    onSalvar: (totalItens: Int) -> Unit,
    onCancelar: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var dadosBrutos by remember { mutableStateOf<Pair<List<String>, List<List<String>>>?>(null) }
    var indexEpc by remember { mutableStateOf<Int?>(null) }
    var indexNome by remember { mutableStateOf<Int?>(null) }
    var indexSetor by remember { mutableStateOf<Int?>(null) }
    var indexLoja by remember { mutableStateOf<Int?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(uri) {
        withContext(Dispatchers.IO) {
            dadosBrutos = LeitorInventario.lerDadosBrutosDaPlanilha(context, uri)
        }
        isLoading = false
        if (dadosBrutos == null) {
            Toast.makeText(context, "Erro ao ler o arquivo ou formato não suportado.", Toast.LENGTH_LONG).show()
            onCancelar()
        }
    }

    val colunas = dadosBrutos?.first ?: emptyList()

    RKTecMiddlewareTheme {
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
            if (isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
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
                                isLoading = true
                                processarEsalvarDados(context, scope, usuarioLogado, uri, dadosBrutos, indexEpc, indexNome, indexSetor, indexLoja,
                                    onSucesso = { total ->
                                        isLoading = false
                                        onSalvar(total)
                                    },
                                    onError = { isLoading = false }
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

// SEU COMPONENTE ORIGINAL, AGORA ESTILIZADO E CORRIGIDO
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

private fun processarEsalvarDados(
    context: Context, scope: CoroutineScope, usuario: String, uri: Uri,
    dadosBrutos: Pair<List<String>, List<List<String>>>?,
    indexEpc: Int?, indexNome: Int?, indexSetor: Int?, indexLoja: Int?,
    onSucesso: (totalItens: Int) -> Unit, onError: () -> Unit
) {
    if (indexEpc == null || dadosBrutos == null) {
        Toast.makeText(context, "A coluna EPC é obrigatória e os dados devem ser válidos.", Toast.LENGTH_SHORT).show()
        onError()
        return
    }

    scope.launch(Dispatchers.IO) {
        try {
            val cabecalho = dadosBrutos.first
            val linhas = dadosBrutos.second

            val mapeamento = MapeamentoPlanilha(
                usuario = usuario,
                nomeArquivo = uri.lastPathSegment ?: "desconhecido",
                colunaEpc = indexEpc,
                colunaNome = indexNome,
                colunaSetor = indexSetor,
                colunaLoja = indexLoja
            )

            val indicesMapeados = listOfNotNull(indexEpc, indexNome, indexSetor, indexLoja)
            val listaItens = linhas.mapNotNull { linha ->
                val tag = linha.getOrNull(indexEpc)?.takeIf { it.isNotBlank() } ?: return@mapNotNull null

                val desc = indexNome?.let { linha.getOrNull(it) } ?: ""
                val setor = indexSetor?.let { linha.getOrNull(it) } ?: ""
                val loja = indexLoja?.let { linha.getOrNull(it) } ?: ""

                val colunasExtras = mutableMapOf<String, String>()
                cabecalho.forEachIndexed { index, nomeColuna ->
                    if (index !in indicesMapeados) {
                        colunasExtras[nomeColuna] = linha.getOrNull(index) ?: ""
                    }
                }
                ItemInventario(tag, desc, setor, loja, colunasExtras)
            }

            if (listaItens.isEmpty()) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Nenhum item válido encontrado na planilha.", Toast.LENGTH_LONG).show()
                }
                onError()
                return@launch
            }

            val db = AppDatabase.getInstance(context)
            db.inventarioDao().limparInventario()
            db.inventarioDao().inserirTodos(listaItens)

            val prefs = context.getSharedPreferences("inventario_prefs", Context.MODE_PRIVATE)
            prefs.edit().putString("cabecalho_original", JSONArray(cabecalho).toString()).apply()

            db.mapeamentoDao().deletarTudo()
            db.mapeamentoDao().inserir(mapeamento)

            withContext(Dispatchers.Main) {
                Toast.makeText(context, "${listaItens.size} itens importados com sucesso!", Toast.LENGTH_SHORT).show()
                onSucesso(listaItens.size)
            }
        } catch (e: Exception) {
            Log.e("ProcessarDados", "Erro na importação", e)
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "Falha na importação: ${e.message}", Toast.LENGTH_LONG).show()
                onError()
            }
        }
    }
}