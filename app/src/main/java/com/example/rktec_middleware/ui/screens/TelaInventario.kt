package com.example.rktec_middleware.ui.screens

import android.net.Uri
import android.webkit.MimeTypeMap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.rktec_middleware.data.db.AppDatabase
import com.example.rktec_middleware.data.model.ItemInventario
import com.example.rktec_middleware.data.model.EpcTag
import com.example.rktec_middleware.util.LeitorInventario
import com.airbnb.lottie.compose.*
import kotlinx.coroutines.launch

@Composable
fun TelaInventario(
    onVoltar: () -> Unit,
    onIniciarLeituraInventario: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val db = remember { AppDatabase.getInstance(context) }

    var nomeArquivo by remember { mutableStateOf<String?>(null) }
    var dadosImportados by remember { mutableStateOf<List<ItemInventario>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var erroImportacao by remember { mutableStateOf<String?>(null) }
    var refresh by remember { mutableStateOf(0) }
    var uriParaMapeamento by remember { mutableStateOf<Uri?>(null) }
    var mostrarTelaMapeamento by remember { mutableStateOf(false) }

    // Busca do banco ao montar ou quando refresh muda
    LaunchedEffect(refresh) {
        val dadosSalvos = db.inventarioDao().listarTodos()
        if (dadosSalvos.isNotEmpty()) {
            dadosImportados = dadosSalvos
            nomeArquivo = "Dados recuperados do banco"
        } else {
            dadosImportados = emptyList()
            nomeArquivo = null
        }
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri: Uri? ->
            uri?.let {
                scope.launch {
                    isLoading = true
                    erroImportacao = null
                    nomeArquivo = it.lastPathSegment

                    // Busca mapeamento salvo
                    val mapeamento = db.mapeamentoDao().buscarPrimeiro()
                    if (mapeamento == null) {
                        // Não tem mapeamento: abre tela de mapeamento e já importa na volta
                        uriParaMapeamento = it
                        mostrarTelaMapeamento = true
                        isLoading = false
                        return@launch
                    }

                    // Já tem mapeamento: lê e importa direto
                    val contentResolver = context.contentResolver
                    val mime = contentResolver.getType(it) ?: ""
                    val extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(mime)?.lowercase()
                        ?: it.lastPathSegment?.substringAfterLast('.', "")?.lowercase()
                        ?: ""

                    val lista = when (extension) {
                        "csv" -> LeitorInventario.lerCsv(context, it, mapeamento)
                        "xls", "xlsx" -> LeitorInventario.lerExcel(context, it, mapeamento)
                        else -> emptyList()
                    }

                    if (lista.isNotEmpty()) {
                        db.inventarioDao().inserirTodos(lista)
                        val epcTags = lista.map { item ->
                            EpcTag(
                                epc = item.tag,
                                descricao = item.desc,
                                setor = item.localizacao,
                                loja = item.loja
                            )
                        }
                        db.coletaDao().inserirTodos(epcTags)
                        dadosImportados = lista
                        nomeArquivo = it.lastPathSegment
                        refresh++
                    } else {
                        erroImportacao = "Planilha inválida!\nVerifique se tem as colunas necessárias."
                        dadosImportados = emptyList()
                        nomeArquivo = null
                    }
                    isLoading = false
                }
            }
        }
    )

    // Tela de mapeamento, se precisar
    if (mostrarTelaMapeamento && uriParaMapeamento != null) {
        TelaMapeamentoPlanilha(
            uri = uriParaMapeamento!!,
            onSalvar = { mapeamento ->
                scope.launch {
                    db.mapeamentoDao().inserir(mapeamento)

                    // Importa já no callback
                    val contentResolver = context.contentResolver
                    val mime = contentResolver.getType(uriParaMapeamento!!) ?: ""
                    val extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(mime)?.lowercase()
                        ?: uriParaMapeamento!!.lastPathSegment?.substringAfterLast('.', "")?.lowercase()
                        ?: ""

                    val lista = when (extension) {
                        "csv" -> LeitorInventario.lerCsv(context, uriParaMapeamento!!, mapeamento)
                        "xls", "xlsx" -> LeitorInventario.lerExcel(context, uriParaMapeamento!!, mapeamento)
                        else -> emptyList()
                    }

                    if (lista.isNotEmpty()) {
                        db.inventarioDao().inserirTodos(lista)
                        val epcTags = lista.map { item ->
                            EpcTag(
                                epc = item.tag,
                                descricao = item.desc,
                                setor = item.localizacao,
                                loja = item.loja
                            )
                        }
                        db.coletaDao().inserirTodos(epcTags)
                        dadosImportados = lista
                        nomeArquivo = uriParaMapeamento!!.lastPathSegment
                        refresh++
                        mostrarTelaMapeamento = false
                    } else {
                        erroImportacao = "Planilha inválida!\nVerifique se tem as colunas necessárias."
                        dadosImportados = emptyList()
                        nomeArquivo = null
                        mostrarTelaMapeamento = false
                    }
                }
            },
            onCancelar = {
                mostrarTelaMapeamento = false
                uriParaMapeamento = null
            }
        )
        return // Não desenha mais nada da TelaInventario quando estiver na tela de mapeamento!
    }

    // ------ UI principal --------
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(top = 32.dp, bottom = 24.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .offset(y = -32.dp)
                .background(
                    Brush.horizontalGradient(
                        listOf(Color(0xFF4A90E2), Color(0xFF174D86))
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            IconButton(
                onClick = onVoltar,
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 8.dp, top = 32.dp)
                    .size(48.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.ArrowBack,
                    contentDescription = "Voltar",
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }

            Text(
                "INVENTÁRIO",
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 32.dp)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(
                onClick = { launcher.launch("*/*") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4A90E2))
            ) {
                Text("Selecionar Planilha (.csv ou .xls)", fontSize = 20.sp, color = Color.White)
            }

            nomeArquivo?.let {
                Text("Arquivo selecionado: $it", fontSize = 16.sp, color = Color.Gray)
            }

            if (isLoading) {
                val composition by rememberLottieComposition(LottieCompositionSpec.Asset("loading.json"))
                val progress by animateLottieCompositionAsState(composition, isPlaying = true)
                LottieAnimation(
                    composition = composition,
                    progress = { progress },
                    modifier = Modifier.size(100.dp)
                )
                Text("Analisando planilha...", color = Color.Gray)
            }

            if (dadosImportados.isEmpty() && !isLoading) {
                Text(
                    "Nenhuma planilha importada.\nImporte uma planilha para começar.",
                    fontWeight = FontWeight.Bold,
                    color = Color.Gray
                )
            }

            if (dadosImportados.isNotEmpty() && !isLoading && erroImportacao == null) {
                Button(
                    onClick = { onIniciarLeituraInventario() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(70.dp),
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
                ) {
                    Text("Iniciar Leitura", fontSize = 20.sp, color = Color.White)
                }
            }
        }
    }

    // ------ DIALOG DE ERRO ------
    if (erroImportacao != null) {
        AlertDialog(
            onDismissRequest = {
                erroImportacao = null
                isLoading = false
                dadosImportados = emptyList()
                refresh++
            },
            title = { Text("Erro ao importar planilha", color = Color.Red, fontWeight = FontWeight.ExtraBold) },
            text = { Text(erroImportacao ?: "") },
            confirmButton = {
                TextButton(onClick = {
                    erroImportacao = null
                    isLoading = false
                    dadosImportados = emptyList()
                    refresh++
                }) {
                    Text("Ok")
                }
            }
        )
    }
}
