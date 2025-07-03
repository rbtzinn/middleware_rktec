package com.example.rktec_middleware.ui.screens

import android.net.Uri
import android.webkit.MimeTypeMap
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
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
    onSalvar: (MapeamentoPlanilha, List<String>) -> Unit,
    onCancelar: () -> Unit
) {
    val context = LocalContext.current
    var colunas by remember { mutableStateOf<List<String>>(emptyList()) }
    var indexEpc by remember { mutableStateOf<Int?>(null) }
    var indexNome by remember { mutableStateOf<Int?>(null) }
    var indexSetor by remember { mutableStateOf<Int?>(null) }
    var indexLoja by remember { mutableStateOf<Int?>(null) }

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

            Text("Coluna do EPC *", fontWeight = FontWeight.Medium)
            DropdownMenuCampo(colunas, indexEpc) { indexEpc = it }

            Text("Coluna do Nome (opcional)", fontWeight = FontWeight.Medium)
            DropdownMenuCampo(colunas, indexNome) { indexNome = it }

            Text("Coluna do Setor (opcional)", fontWeight = FontWeight.Medium)
            DropdownMenuCampo(colunas, indexSetor) { indexSetor = it }

            Text("Coluna da Loja (opcional)", fontWeight = FontWeight.Medium)
            DropdownMenuCampo(colunas, indexLoja) { indexLoja = it }

            Spacer(Modifier.height(24.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Button(
                    enabled = indexEpc != null,
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4A90E2)),
                    onClick = {
                        onSalvar(
                            MapeamentoPlanilha(
                                usuario = "",
                                nomeArquivo = uri.lastPathSegment ?: "",
                                colunaEpc = indexEpc!!,
                                colunaNome = indexNome,
                                colunaSetor = indexSetor,
                                colunaLoja = indexLoja
                            ),
                            colunas
                        )
                    }
                ) {
                    Text("Salvar", fontSize = 18.sp, color = Color.White)
                }
                OutlinedButton(
                    shape = RoundedCornerShape(28.dp),
                    onClick = onCancelar
                ) {
                    Text("Cancelar", fontSize = 18.sp)
                }
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
    val displayText = if (selecionado != null && selecionado in colunas.indices) colunas[selecionado] else "Selecione"

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentSize(Alignment.TopStart)
    ) {
        OutlinedButton(
            onClick = { expanded = true },
            shape = RoundedCornerShape(28.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = Color.White,
                contentColor = Color(0xFF1A6DB0)
            ),
            border = ButtonDefaults.outlinedButtonBorder.copy(
                width = 1.dp
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = displayText,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF212529),
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = Icons.Filled.ArrowDropDown,
                    contentDescription = "Abrir menu",
                    tint = Color(0xFF6C757D)
                )
            }
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 300.dp) // LIMITA ALTURA!
                .background(Color.White)
        ) {
            DropdownMenuItem(
                text = { Text("Nenhum") },
                onClick = {
                    onSelecionado(null)
                    expanded = false
                }
            )
            colunas.forEachIndexed { idx, col ->
                DropdownMenuItem(
                    text = { Text(col) },
                    onClick = {
                        onSelecionado(idx)
                        expanded = false
                    }
                )
            }
        }
    }
}

