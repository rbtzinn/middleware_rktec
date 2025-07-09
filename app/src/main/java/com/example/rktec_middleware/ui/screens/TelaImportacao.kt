package com.example.rktec_middleware.ui.screens

import android.Manifest
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
import kotlinx.coroutines.launch
import com.example.rktec_middleware.ui.screens.TelaMapeamentoPlanilha

@Composable
fun TelaImportacao(
    onConcluido: (String) -> Unit,
    appDatabase: AppDatabase,
    usuario: String,
    onDebugClick: () -> Unit,
    onSobreClick: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var uriParaMapeamento by remember { mutableStateOf<Uri?>(null) }
    var erro by remember { mutableStateOf<String?>(null) }
    var permissaoConcedida by remember { mutableStateOf(true) }

    // 1. Lançador de permissão para Android < 10
    val requestPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            permissaoConcedida = granted
            if (!granted) {
                erro = "Permissão de armazenamento negada. Não é possível salvar arquivos de relatório."
            }
        }
    )

    // 2. Pede permissão logo que entra na tela (Android < 10)
    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            permissaoConcedida = false
            requestPermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
    }

    // Lançador pra escolher arquivo
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri: Uri? ->
            if (uri != null) {
                uriParaMapeamento = uri
            } else {
                erro = "Nenhum arquivo selecionado"
            }
        }
    )

    if (uriParaMapeamento != null) {
        TelaMapeamentoPlanilha(
            uri = uriParaMapeamento!!,
            usuarioLogado = usuario,
            onSalvar = { mapeamento, nomesColunas ->
                scope.launch {
                    val contentResolver = context.contentResolver
                    val mime = contentResolver.getType(uriParaMapeamento!!) ?: ""
                    val extension = android.webkit.MimeTypeMap.getSingleton()
                        .getExtensionFromMimeType(mime)?.lowercase()
                        ?: uriParaMapeamento!!.lastPathSegment?.substringAfterLast('.', "")?.lowercase()
                        ?: ""

                    val lista = when (extension) {
                        "csv" -> com.example.rktec_middleware.util.LeitorInventario.lerCsv(context, uriParaMapeamento!!, mapeamento)
                        "xls", "xlsx" -> com.example.rktec_middleware.util.LeitorInventario.lerExcel(context, uriParaMapeamento!!, mapeamento)
                        else -> emptyList()
                    }

                    if (lista.isNotEmpty()) {
                        appDatabase.inventarioDao().inserirTodos(lista)
                        val epcTags = lista.map { item ->
                            com.example.rktec_middleware.data.model.EpcTag(
                                epc = item.tag,
                                descricao = item.desc,
                                setor = item.localizacao,
                                loja = item.loja,
                                timestamp = System.currentTimeMillis()
                            )
                        }
                        appDatabase.coletaDao().inserirTodos(epcTags)
                    }

                    appDatabase.mapeamentoDao().inserir(mapeamento)

                    com.example.rktec_middleware.util.LogHelper.exportarRelatorioMapeamentoXlsx(
                        context = context,
                        usuario = usuario,
                        arquivo = uriParaMapeamento!!.lastPathSegment ?: "",
                        mapeamento = mapeamento,
                        nomesColunas = nomesColunas
                    )

                    uriParaMapeamento = null
                    onConcluido("relatorio mapeamento.xlsx")
                }
            },
            onCancelar = { uriParaMapeamento = null }
        )
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(top = 32.dp, bottom = 24.dp)
    ) {
        // Cabeçalho igual ao da tela principal
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
            Text(
                "Importação de Planilha",
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 32.dp)
            )
        }

        // Conteúdo central da tela
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                "Importe sua planilha para começar",
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp,
                color = Color(0xFF174D86)
            )
            Spacer(Modifier.height(20.dp))
            Button(
                onClick = { launcher.launch("*/*") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4A90E2)),
                enabled = permissaoConcedida // Só habilita se tem permissão
            ) {
                Text("Selecionar planilha (.csv ou .xls)", fontSize = 20.sp, color = Color.White)
            }
            erro?.let {
                Spacer(Modifier.height(8.dp))
                Text(it, color = Color.Red)
            }
        }

        Divider(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            color = Color(0xFFE0E0E0),
            thickness = 1.dp
        )

        // Footer igual ao padrão
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        ) {
            TextButton(
                onClick = onDebugClick,
            ) {
                Text(
                    "Ver debug de coletas",
                    fontSize = 14.sp,
                    color = Color.Gray,
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            TextButton(
                onClick = onSobreClick,
            ) {
                Text(
                    "RKTECNOLOGIAS",
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF009688),
                    fontSize = 16.sp
                )
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                "Todos os direitos reservados — RKTECNOLOGIAS",
                fontSize = 12.sp,
                color = Color.Gray,
            )
        }
    }
}
