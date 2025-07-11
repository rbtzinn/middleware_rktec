package com.example.rktec_middleware.ui.screens

import android.Manifest
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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

    val requestPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            permissaoConcedida = granted
            if (!granted) {
                erro = "Permissão de armazenamento negada."
            }
        }
    )

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            permissaoConcedida = false
            requestPermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri: Uri? ->
            uri?.let { uriParaMapeamento = it } ?: run { erro = "Nenhum arquivo selecionado" }
        }
    )

    if (uriParaMapeamento != null) {
        TelaMapeamentoPlanilha(
            uri = uriParaMapeamento!!,
            usuarioLogado = usuario,
            onSalvar = { mapeamento, nomesColunas ->
                scope.launch {
                    val mime = context.contentResolver.getType(uriParaMapeamento!!) ?: ""
                    val ext = android.webkit.MimeTypeMap.getSingleton()
                        .getExtensionFromMimeType(mime)?.lowercase()
                        ?: uriParaMapeamento!!.lastPathSegment?.substringAfterLast('.')?.lowercase()
                        ?: ""

                    val lista = when (ext) {
                        "csv" -> com.example.rktec_middleware.util.LeitorInventario.lerCsv(context, uriParaMapeamento!!, mapeamento)
                        "xls", "xlsx" -> com.example.rktec_middleware.util.LeitorInventario.lerExcel(context, uriParaMapeamento!!, mapeamento)
                        else -> emptyList()
                    }

                    if (lista.isNotEmpty()) {
                        appDatabase.inventarioDao().inserirTodos(lista)
                        val epcs = lista.map {
                            com.example.rktec_middleware.data.model.EpcTag(
                                epc = it.tag,
                                descricao = it.desc,
                                setor = it.localizacao,
                                loja = it.loja,
                                timestamp = System.currentTimeMillis()
                            )
                        }
                        appDatabase.coletaDao().inserirTodos(epcs)
                    }

                    appDatabase.mapeamentoDao().inserir(mapeamento)
                    com.example.rktec_middleware.util.LogHelper.exportarRelatorioMapeamentoXlsx(
                        context,
                        usuario,
                        uriParaMapeamento!!.lastPathSegment ?: "",
                        mapeamento,
                        nomesColunas
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
            .background(Color(0xFFF5F7FA))
    ) {
        // Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(110.dp)
                .background(
                    Brush.verticalGradient(
                        0f to Color(0xFF174D86),
                        1f to Color(0xFF4A90E2)
                    )
                )
        ) {
            Row(
                Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Importar Planilha",
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.width(44.dp))
            }
        }

        // Card explicativo e ação
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .offset(y = (-26).dp)
                .padding(horizontal = 18.dp),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    "Importe a planilha base de inventário",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 18.sp,
                    color = Color(0xFF174D86)
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "O sistema vai considerar os dados dessa planilha como base para o inventário. Ela será usada para identificar os itens e construir o banco de dados.",
                    fontSize = 15.sp,
                    color = Color(0xFF555555)
                )
                Spacer(Modifier.height(20.dp))
                Button(
                    onClick = { launcher.launch("*/*") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4A90E2)),
                    enabled = permissaoConcedida
                ) {
                    Text(
                        "Selecionar planilha (.csv ou .xls)",
                        fontSize = 16.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
                erro?.let {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(it, color = Color.Red, fontSize = 14.sp)
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Footer
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp)
        ) {
            Spacer(Modifier.height(4.dp))
            TextButton(onClick = onSobreClick) {
                Text("RKTECNOLOGIAS", fontWeight = FontWeight.Bold, color = Color(0xFF009688), fontSize = 15.sp)
            }
            Text(
                "Todos os direitos reservados — RKTECNOLOGIAS",
                fontSize = 12.sp,
                color = Color.Gray
            )
        }
    }
}