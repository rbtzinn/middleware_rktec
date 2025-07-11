package com.example.rktec_middleware.ui.screens

import android.Manifest
import android.app.Activity
import android.net.Uri
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.rktec_middleware.data.model.MapeamentoPlanilha
import com.example.rktec_middleware.util.LogHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.apache.poi.ss.usermodel.WorkbookFactory
import java.io.BufferedReader
import java.io.InputStreamReader

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TelaMapeamentoPlanilha(
    uri: Uri,
    usuarioLogado: String,
    onSalvar: (MapeamentoPlanilha, List<String>) -> Unit,
    onCancelar: () -> Unit
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val scope = rememberCoroutineScope()
    var colunas by remember { mutableStateOf<List<String>>(emptyList()) }
    var indexEpc by remember { mutableStateOf<Int?>(null) }
    var indexNome by remember { mutableStateOf<Int?>(null) }
    var indexSetor by remember { mutableStateOf<Int?>(null) }
    var indexLoja by remember { mutableStateOf<Int?>(null) }
    var erroPermissao by remember { mutableStateOf<String?>(null) }

    // Permissão para Android 9
    val requestPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            if (granted) {
                salvarMapeamento(
                    context, scope, usuarioLogado, uri, colunas, indexEpc, indexNome, indexSetor, indexLoja, onSalvar
                )
            } else {
                erroPermissao = "Permissão de armazenamento negada. Não foi possível salvar o relatório."
            }
        }
    )

    // Detecta colunas
    LaunchedEffect(uri) {
        val fileName = uri.lastPathSegment ?: ""
        val contentResolver = context.contentResolver
        val mime = contentResolver.getType(uri) ?: ""
        val extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(mime)?.lowercase()
            ?: fileName.substringAfterLast('.', "").lowercase()
            ?: ""
        val inputStream = contentResolver.openInputStream(uri)
        val colunasDetectadas = when (extension) {
            "csv" -> {
                val reader = BufferedReader(InputStreamReader(inputStream, Charsets.UTF_8))
                var primeiraLinha: String? = null
                while (primeiraLinha == null && reader.ready()) {
                    val linha = reader.readLine()
                    if (linha != null && linha.isNotBlank()) primeiraLinha = linha
                }
                if (primeiraLinha == null) emptyList()
                else {
                    val delimitadores = listOf(";", ",", "\t", "|")
                    val melhorDelim = delimitadores.maxByOrNull { primeiraLinha.count { ch -> ch == it[0] } } ?: ","
                    primeiraLinha.split(melhorDelim)
                        .map { it.trim().replace("\"", "") }
                        .filter { it.isNotBlank() }
                }
            }
            "xls", "xlsx" -> {
                val workbook = WorkbookFactory.create(inputStream)
                val sheet = workbook.getSheetAt(0)
                val headerRow = sheet.getRow(0)
                headerRow?.map { it.toString().trim() }?.filter { it.isNotBlank() } ?: emptyList()
            }
            else -> emptyList()
        }
        colunas = colunasDetectadas
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFFF8FAFF)
    ) {
        Scaffold(
            topBar = {
                Column {
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
                                "Mapeamento de Planilha",
                                color = Color.White,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(1f),
                                textAlign = TextAlign.Center,
                                letterSpacing = 0.5.sp
                            )
                            Spacer(modifier = Modifier.width(44.dp))
                        }
                    }
                }
            },
            containerColor = Color.Transparent
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header informativo
                Card(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFE3F2FD)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF4A90E2))
                                .padding(8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = "Informação",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Text(
                            text = "Selecione a correspondência entre as colunas do arquivo e os campos do sistema",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF2C3E50),
                            modifier = Modifier.weight(1f))
                    }
                }

                // Card de mapeamento
                Card(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    elevation = CardDefaults.cardElevation(6.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp)
                    ) {
                        // Campos de mapeamento
                        CampoMapeamentoPremium(
                            titulo = "Coluna do EPC",
                            obrigatorio = true,
                            descricao = "Identificador único do ativo",
                            colunas = colunas,
                            selecionado = indexEpc,
                            onSelecionado = { indexEpc = it }
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        CampoMapeamentoPremium(
                            titulo = "Coluna do Nome",
                            obrigatorio = false,
                            descricao = "Nome descritivo do ativo",
                            colunas = colunas,
                            selecionado = indexNome,
                            onSelecionado = { indexNome = it }
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        CampoMapeamentoPremium(
                            titulo = "Coluna do Setor",
                            obrigatorio = false,
                            descricao = "Localização física do ativo",
                            colunas = colunas,
                            selecionado = indexSetor,
                            onSelecionado = { indexSetor = it }
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        CampoMapeamentoPremium(
                            titulo = "Coluna da Loja",
                            obrigatorio = false,
                            descricao = "Unidade empresarial",
                            colunas = colunas,
                            selecionado = indexLoja,
                            onSelecionado = { indexLoja = it }
                        )
                    }
                }

                // Área de ações
                Column(
                    modifier = Modifier
                        .padding(24.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Button(
                        enabled = indexEpc != null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF4A90E2),
                            disabledContainerColor = Color(0xFFB0C4DE)
                        ),
                        elevation = ButtonDefaults.buttonElevation(
                            defaultElevation = 4.dp,
                            pressedElevation = 8.dp
                        ),
                        onClick = {
                            erroPermissao = null
                            if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.Q) {
                                val granted = context.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == android.content.pm.PackageManager.PERMISSION_GRANTED
                                if (!granted) {
                                    requestPermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                                } else {
                                    salvarMapeamento(
                                        context, scope, usuarioLogado, uri, colunas, indexEpc, indexNome, indexSetor, indexLoja, onSalvar
                                    )
                                }
                            } else {
                                salvarMapeamento(
                                    context, scope, usuarioLogado, uri, colunas, indexEpc, indexNome, indexSetor, indexLoja, onSalvar
                                )
                            }
                        }
                    ) {
                        Text(
                            "CONFIRMAR MAPEAMENTO",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    TextButton(
                        onClick = onCancelar,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            "CANCELAR",
                            color = Color(0xFF7F8C8D),
                            fontWeight = FontWeight.Medium
                        )
                    }

                    erroPermissao?.let {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = it,
                            color = Color(0xFFE74C3C),
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .padding(8.dp)
                                .fillMaxWidth()
                        )
                    }
                }

                // Footer informativo
                Surface(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFF1F8FF),
                    shadowElevation = 2.dp
                ) {
                    Row(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Arquivo selecionado:",
                            color = Color(0xFF7F8C8D),
                            style = MaterialTheme.typography.labelMedium,
                            modifier = Modifier.weight(1f)
                        )

                        Text(
                            text = uri.lastPathSegment?.takeLast(30) ?: "N/A",
                            color = Color(0xFF2C3E50),
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CampoMapeamentoPremium(
    titulo: String,
    obrigatorio: Boolean,
    descricao: String,
    colunas: List<String>,
    selecionado: Int?,
    onSelecionado: (Int?) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = titulo,
                style = MaterialTheme.typography.titleSmall,
                color = Color(0xFF2C3E50),
                fontWeight = FontWeight.SemiBold
            )

            if (obrigatorio) {
                Text(
                    text = " *",
                    color = Color(0xFFE74C3C),
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = descricao,
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFF7F8C8D)
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Campo de seleção premium
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentSize(Alignment.TopStart)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = true },
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFFF8FBFF),
                border = null,
                shadowElevation = 2.dp
            ) {
                Row(
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 18.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = selecionado?.let { colunas.getOrNull(it) } ?: "Selecione a coluna",
                        color = if (selecionado == null) Color(0xFF95A5A6) else Color(0xFF2C3E50),
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = "Abrir opções",
                        tint = Color(0xFF4A90E2)
                    )
                }
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .heightIn(max = 280.dp)
                    .background(Color.White)
            ) {
                // Opção para deselecionar
                DropdownMenuItem(
                    text = {
                        Text(
                            "Nenhuma seleção",
                            color = Color(0xFF7F8C8D)
                        )
                    },
                    onClick = {
                        onSelecionado(null)
                        expanded = false
                    },
                    trailingIcon = {
                        if (selecionado == null) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Selecionado",
                                tint = Color(0xFF4A90E2)
                            )
                        }
                    }
                )

                Divider()

                // Opções de colunas
                colunas.forEachIndexed { idx, col ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = col,
                                fontWeight = if (selecionado == idx) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        onClick = {
                            onSelecionado(idx)
                            expanded = false
                        },
                        trailingIcon = {
                            if (selecionado == idx) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Selecionado",
                                    tint = Color(0xFF4A90E2)
                                )
                            }
                        }
                    )
                }
            }
        }
    }
}

private fun salvarMapeamento(
    context: android.content.Context,
    scope: CoroutineScope,
    usuario: String,
    uri: Uri,
    colunas: List<String>,
    indexEpc: Int?,
    indexNome: Int?,
    indexSetor: Int?,
    indexLoja: Int?,
    onSalvar: (MapeamentoPlanilha, List<String>) -> Unit
) {
    scope.launch {
        val nomeArquivo = uri.lastPathSegment ?: ""
        val mapeamento = MapeamentoPlanilha(
            usuario = usuario,
            nomeArquivo = nomeArquivo,
            colunaEpc = indexEpc!!,
            colunaNome = indexNome,
            colunaSetor = indexSetor,
            colunaLoja = indexLoja
        )
        onSalvar(mapeamento, colunas)
        LogHelper.registrarMapeamento(context, usuario, nomeArquivo)
        val arquivo = LogHelper.exportarRelatorioMapeamentoXlsx(context, usuario, nomeArquivo, mapeamento, colunas)
        if (arquivo != null) {
            Toast.makeText(
                context,
                "Relatório salvo em:\n${arquivo.absolutePath}",
                Toast.LENGTH_LONG
            ).show()
        } else {
            Toast.makeText(
                context,
                "Erro ao salvar arquivo! Verifique as permissões de armazenamento do app.",
                Toast.LENGTH_LONG
            ).show()
        }
    }
}