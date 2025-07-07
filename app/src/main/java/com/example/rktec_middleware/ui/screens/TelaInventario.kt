@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.rktec_middleware.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.rktec_middleware.data.db.AppDatabase
import com.example.rktec_middleware.data.model.ItemInventario

@Composable
fun TelaInventario(
    onVoltar: () -> Unit,
    onIniciarLeituraInventario: (
        filtroLoja: String?,
        filtroSetor: String?,
        listaTotal: List<ItemInventario>,
        listaFiltrada: List<ItemInventario>
    ) -> Unit,
    onDebugClick: () -> Unit,
    onSobreClick: () -> Unit
) {
    val context = LocalContext.current
    val db = remember { AppDatabase.getInstance(context) }

    var dadosImportados by remember { mutableStateOf<List<ItemInventario>>(emptyList()) }
    var refresh by remember { mutableStateOf(0) }

    var filtroLoja by remember { mutableStateOf<String?>(null) }
    var filtroSetor by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(refresh) {
        val dadosSalvos = db.inventarioDao().listarTodos()
        dadosImportados = if (dadosSalvos.isNotEmpty()) dadosSalvos else emptyList()
    }

    val lojas = dadosImportados
        .map { it.loja.trim() }
        .filter { it.isNotBlank() }
        .distinct()

    val setores = dadosImportados
        .map { it.localizacao.trim() }
        .filter { it.isNotBlank() }
        .map {
            it.toDoubleOrNull()?.let { d ->
                if (d % 1.0 == 0.0) d.toInt().toString() else it
            } ?: it
        }
        .distinct()

    val listaFiltrada = dadosImportados.filter { item ->
        (filtroLoja.isNullOrEmpty() || item.loja.trim() == filtroLoja) &&
                (filtroSetor.isNullOrEmpty() || (item.localizacao.trim().toDoubleOrNull()?.let { d -> if (d % 1.0 == 0.0) d.toInt().toString() else item.localizacao.trim() } ?: item.localizacao.trim()) == filtroSetor)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F7FA))
    ) {
        // Header moderno
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
                IconButton(
                    onClick = onVoltar,
                    modifier = Modifier
                        .size(44.dp)
                        .background(Color.White.copy(alpha = 0.20f), CircleShape)
                ) {
                    Icon(
                        Icons.Filled.ArrowBack,
                        contentDescription = "Voltar",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Inventário",
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

        // Painel de filtro compacto
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .offset(y = (-26).dp)
                .padding(horizontal = 18.dp)
                .shadow(elevation = 7.dp, shape = RoundedCornerShape(18.dp)),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    "Filtros rápidos",
                    color = Color(0xFF174D86),
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    modifier = Modifier.padding(bottom = 7.dp)
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    DropdownFiltroProfissional(
                        label = "Loja",
                        opcoes = lojas,
                        selecionado = filtroLoja,
                        onSelecionado = { filtroLoja = it },
                        modifier = Modifier.weight(1f)
                    )
                    DropdownFiltroProfissional(
                        label = "Setor",
                        opcoes = setores,
                        selecionado = filtroSetor,
                        onSelecionado = { filtroSetor = it },
                        modifier = Modifier.weight(1f)
                    )
                }

                Text(
                    "Itens: ${listaFiltrada.size} / ${dadosImportados.size}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = Color(0xFF4A90E2),
                    modifier = Modifier
                        .align(Alignment.End)
                        .padding(top = 8.dp)
                )
            }
        }

        // Conteúdo da lista de inventário
        Box(
            Modifier
                .weight(1f)
                .padding(top = 8.dp, start = 16.dp, end = 16.dp, bottom = 0.dp)
        ) {
            if (dadosImportados.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Nenhum item de inventário encontrado",
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF9CA3AF),
                        fontSize = 17.sp,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(listaFiltrada) { item ->
                        ItemInventarioCardNovo(item)
                    }
                }
            }
        }

        // Botão principal na base (float)
        if (dadosImportados.isNotEmpty()) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Button(
                    onClick = {
                        onIniciarLeituraInventario(
                            filtroLoja,
                            filtroSetor,
                            dadosImportados,
                            listaFiltrada
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4A90E2))
                ) {
                    Text(
                        "Iniciar Leitura",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp,
                        color = Color.White
                    )
                }
            }
        }

        // Footer minimalista
        Text(
            "RKTECNOLOGIAS",
            color = Color(0xFF009688),
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 10.dp)
                .clickable(onClick = onSobreClick),
            textAlign = TextAlign.Center
        )
    }
}

// Card modernizado para item do inventário
@Composable
fun ItemInventarioCardNovo(item: ItemInventario) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation = 3.dp, shape = RoundedCornerShape(14.dp)),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 13.dp, horizontal = 18.dp)
        ) {
            Text(
                text = item.desc.ifBlank { "Sem descrição" },
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp,
                color = Color(0xFF222222),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = item.tag,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = Color(0xFF4A90E2),
                modifier = Modifier.padding(top = 2.dp)
            )
            Row(
                Modifier
                    .padding(top = 8.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (item.localizacao.isNotBlank()) {
                    InfoChipNovo("Setor: ${item.localizacao}")
                }
                if (item.loja.isNotBlank()) {
                    InfoChipNovo("Loja: ${item.loja}")
                }
            }
        }
    }
}

// Chip info padronizado (clean)
@Composable
fun InfoChipNovo(text: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFFE9F2FA))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = text,
            color = Color(0xFF1A6DB0),
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

// Dropdown clean e compacto
@Composable
fun DropdownFiltroProfissional(
    label: String,
    opcoes: List<String>,
    selecionado: String?,
    onSelecionado: (String?) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val displayText = if (selecionado.isNullOrEmpty()) "Todos" else selecionado

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = displayText,
            onValueChange = {},
            label = { Text(label, fontSize = 12.sp, color = Color(0xFF174D86)) },
            readOnly = true,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            modifier = Modifier
                .menuAnchor()
                .height(54.dp) // Altura menor!
                .clip(RoundedCornerShape(10.dp)),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = Color(0xFFE0E0E0),
                focusedBorderColor = Color(0xFF4A90E2),
                focusedLabelColor = Color(0xFF174D86),
                cursorColor = Color.Transparent,
                disabledBorderColor = Color(0xFFE0E0E0),
                unfocusedContainerColor = Color.White
            ),
            singleLine = true,
            textStyle = LocalTextStyle.current.copy(fontSize = 15.sp)
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .background(Color.White)
                .widthIn(min = 110.dp, max = 220.dp)
        ) {
            DropdownMenuItem(
                text = { Text("Todos", fontWeight = if (selecionado == null) FontWeight.Bold else FontWeight.Normal) },
                onClick = {
                    onSelecionado(null)
                    expanded = false
                }
            )
            opcoes.forEach { opcao ->
                DropdownMenuItem(
                    text = { Text(opcao, fontWeight = if (selecionado == opcao) FontWeight.Bold else FontWeight.Normal) },
                    onClick = {
                        onSelecionado(opcao)
                        expanded = false
                    }
                )
            }
        }
    }
}
