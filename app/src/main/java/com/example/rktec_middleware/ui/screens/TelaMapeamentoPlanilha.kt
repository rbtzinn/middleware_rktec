package com.example.rktec_middleware.ui.screens

import android.net.Uri
import android.webkit.MimeTypeMap
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
import com.example.rktec_middleware.data.model.MapeamentoPlanilha
import org.apache.poi.ss.usermodel.WorkbookFactory
import java.io.BufferedReader
import java.io.InputStreamReader

@Composable
fun TelaMapeamentoPlanilha(
    uri: Uri,
    onSalvar: (MapeamentoPlanilha) -> Unit,
    onCancelar: () -> Unit
) {
    val context = LocalContext.current
    var colunas by remember { mutableStateOf<List<String>>(emptyList()) }
    var indexEpc by remember { mutableStateOf<Int?>(null) }
    var indexNome by remember { mutableStateOf<Int?>(null) }
    var indexSetor by remember { mutableStateOf<Int?>(null) }
    var indexLoja by remember { mutableStateOf<Int?>(null) }

    // Carrega nomes das colunas ao montar
    LaunchedEffect(uri) {
        val fileName = uri.lastPathSegment ?: ""
        val contentResolver = context.contentResolver
        val mime = contentResolver.getType(uri) ?: ""
        val extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(mime)?.lowercase()
            ?: fileName.substringAfterLast('.', "").lowercase()
            ?: ""
        println("DEBUG: Arquivo selecionado: $fileName, extensão detectada: $extension, mime: $mime")

        val inputStream = contentResolver.openInputStream(uri)
        val colunasDetectadas = when (extension) {
            "csv" -> {
                val reader = BufferedReader(InputStreamReader(inputStream, Charsets.UTF_8))
                // Pula linhas em branco até encontrar a primeira não vazia
                var primeiraLinha: String? = null
                while (primeiraLinha == null && reader.ready()) {
                    val linha = reader.readLine()
                    if (linha != null && linha.isNotBlank()) primeiraLinha = linha
                }
                if (primeiraLinha == null) {
                    println("CSV: NENHUMA LINHA ENCONTRADA NO CABEÇALHO!")
                    emptyList()
                } else {
                    val delimitadores = listOf(";", ",", "\t", "|")
                    val melhorDelim = delimitadores.maxByOrNull { primeiraLinha.count { ch -> ch == it[0] } } ?: ","
                    val cols = primeiraLinha.split(melhorDelim)
                        .map { it.trim().replace("\"", "") }
                        .filter { it.isNotBlank() }
                    println("CSV: Colunas detectadas: $cols")
                    cols
                }
            }
            "xls", "xlsx" -> {
                val workbook = WorkbookFactory.create(inputStream)
                val sheet = workbook.getSheetAt(0)
                val headerRow = sheet.getRow(0)
                val cols = headerRow?.map { it.toString().trim() }?.filter { it.isNotBlank() } ?: emptyList()
                println("EXCEL: Colunas detectadas: $cols")
                cols
            }
            else -> {
                println("Arquivo sem extensão suportada: $extension")
                emptyList()
            }
        }
        colunas = colunasDetectadas
    }

    // ---- UI estilizada padrão RKTEC ----
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
            Text(
                "MAPEAMENTO",
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 32.dp)
            )
        }

        Spacer(Modifier.height(32.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Selecione qual coluna representa cada campo:", fontWeight = FontWeight.Bold)

            // EPC - Obrigatório
            Text("Coluna do EPC *", fontWeight = FontWeight.Medium)
            DropdownMenuCampo(
                colunas = colunas,
                selecionado = indexEpc,
                onSelecionado = { indexEpc = it }
            )

            // Nome - Opcional
            Text("Coluna do Nome (opcional)", fontWeight = FontWeight.Medium)
            DropdownMenuCampo(
                colunas = colunas,
                selecionado = indexNome,
                onSelecionado = { indexNome = it }
            )

            // Setor - Opcional
            Text("Coluna do Setor (opcional)", fontWeight = FontWeight.Medium)
            DropdownMenuCampo(
                colunas = colunas,
                selecionado = indexSetor,
                onSelecionado = { indexSetor = it }
            )

            // Loja - Opcional
            Text("Coluna da Loja (opcional)", fontWeight = FontWeight.Medium)
            DropdownMenuCampo(
                colunas = colunas,
                selecionado = indexLoja,
                onSelecionado = { indexLoja = it }
            )

            Spacer(Modifier.height(24.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Button(
                    enabled = indexEpc != null,
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4A90E2)),
                    onClick = {
                        onSalvar(
                            MapeamentoPlanilha(
                                usuario = "", // preenche se usar login
                                nomeArquivo = uri.lastPathSegment ?: "",
                                colunaEpc = indexEpc!!,
                                colunaNome = indexNome,
                                colunaSetor = indexSetor,
                                colunaLoja = indexLoja
                            )
                        )
                    }
                ) { Text("Salvar", fontSize = 18.sp, color = Color.White) }
                OutlinedButton(
                    shape = RoundedCornerShape(28.dp),
                    onClick = onCancelar
                ) { Text("Cancelar", fontSize = 18.sp) }
            }
        }
    }
}

@Composable
fun DropdownMenuCampo(
    colunas: List<String>,
    selecionado: Int?,
    onSelecionado: (Int?) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
    ) {
        OutlinedButton(
            onClick = { expanded = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                if (selecionado != null && selecionado in colunas.indices) colunas[selecionado] else "Selecione"
            )
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            colunas.forEachIndexed { idx, col ->
                DropdownMenuItem(
                    text = { Text(col) },
                    onClick = {
                        onSelecionado(idx)
                        expanded = false
                    }
                )
            }
            DropdownMenuItem(
                text = { Text("Nenhum") },
                onClick = {
                    onSelecionado(null)
                    expanded = false
                }
            )
        }
    }
}
