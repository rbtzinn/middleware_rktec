package com.example.rktec_middleware.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.material3.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.style.TextAlign
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

    val lojas = dadosImportados.map { it.loja }.filter { it.isNotBlank() }.distinct()
    val setores = dadosImportados.map { it.localizacao }.filter { it.isNotBlank() }.distinct()

    val listaFiltrada = dadosImportados.filter { item ->
        (filtroLoja.isNullOrEmpty() || item.loja == filtroLoja) &&
                (filtroSetor.isNullOrEmpty() || item.localizacao == filtroSetor)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FC))
    ) {
        // Cabeçalho com gradiente profissional
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp)
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color(0xFF1A6DB0),
                            Color(0xFF0D4A82),
                            Color(0xFF083A6C)
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Row(
                Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onVoltar,
                    modifier = Modifier.padding(start = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBack,
                        contentDescription = "Voltar",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }
                Text(
                    "INVENTÁRIO",
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )
            }
        }

        // Painel de filtros com sombra suave
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
                .shadow(
                    elevation = 8.dp,
                    shape = RoundedCornerShape(16.dp)
                ),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier.padding(12.dp)
            ) {
                Text(
                    "Filtrar Itens",
                    color = Color(0xFF0D4A82),
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 18.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (lojas.isNotEmpty()) {
                        Box(modifier = Modifier.weight(1f)) {
                            DropdownFiltroEstilizado(
                                label = "Loja",
                                opcoes = lojas,
                                selecionado = filtroLoja,
                                onSelecionado = { filtroLoja = it }
                            )
                        }
                    }
                    if (setores.isNotEmpty()) {
                        Box(modifier = Modifier.weight(1f)) {
                            DropdownFiltroEstilizado(
                                label = "Setor",
                                opcoes = setores,
                                selecionado = filtroSetor,
                                onSelecionado = { filtroSetor = it }
                            )
                        }
                    }
                }
                // Contagem itens com destaque
                Text(
                    "Itens: ${listaFiltrada.size} de ${dadosImportados.size}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = Color(0xFF1A6DB0),
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(top = 8.dp)
                )
            }
        }

        // Lista de itens com cards modernos
        if (dadosImportados.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "Nenhum item de inventário encontrado",
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF6C757D),
                    fontSize = 18.sp
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp)
            ) {
                items(listaFiltrada) { item ->
                    ItemInventarioCard(item = item)
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Botão principal com efeito visual
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
                    .height(56.dp)
                    .padding(horizontal = 16.dp)
                    .shadow(
                        elevation = 6.dp,
                        shape = RoundedCornerShape(28.dp)
                    ),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF1A6DB0)
                )
            ) {
                Text(
                    "INICIAR LEITURA",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.8.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Footer minimalista
        Text(
            "RKTECNOLOGIAS",
            color = Color(0xFF6C757D),
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
                .clickable(onClick = onSobreClick),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun ItemInventarioCard(item: ItemInventario) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation = 2.dp, shape = RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
        ) {
            Text(
                text = item.tag,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = Color(0xFF212529),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            if (item.desc.isNotBlank()) {
                Text(
                    item.desc,
                    fontSize = 14.sp,
                    color = Color(0xFF495057),
                    modifier = Modifier.padding(top = 4.dp),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Row(
                modifier = Modifier.padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (item.localizacao.isNotBlank()) {
                    InfoChip("Setor: ${item.localizacao}")
                }
                if (item.loja.isNotBlank()) {
                    InfoChip("Loja: ${item.loja}")
                }
            }
        }
    }
}

@Composable
fun InfoChip(text: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFFE9ECEF))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = text,
            color = Color(0xFF495057),
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun DropdownFiltroEstilizado(
    label: String,
    opcoes: List<String>,
    selecionado: String?,
    onSelecionado: (String?) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val displayText = if (selecionado.isNullOrEmpty()) "Todos" else selecionado

    Box(
        modifier = Modifier
            .height(52.dp)
    ) {
        OutlinedButton(
            onClick = { expanded = true },
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = Color.White,
                contentColor = Color(0xFF1A6DB0)
            ),
            border = ButtonDefaults.outlinedButtonBorder.copy(
                width = 1.2.dp
            ),
            modifier = Modifier.fillMaxSize()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.Start
                ) {
                    Text(
                        label,
                        color = Color(0xFF6C757D),
                        fontSize = 12.sp,
                        modifier = Modifier.padding(bottom = 2.dp)
                    )
                    Text(
                        displayText,
                        color = Color(0xFF212529),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Icon(
                    imageVector = Icons.Filled.ArrowDropDown,
                    contentDescription = "Abrir filtro",
                    tint = Color(0xFF6C757D)
                )
            }
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .widthIn(min = 200.dp)
                .heightIn(max = 280.dp)
                .background(Color.White)
        ) {
            DropdownMenuItem(
                text = {
                    Text(
                        "Todos",
                        fontWeight = if (selecionado == null) FontWeight.Bold else FontWeight.Normal,
                        color = if (selecionado == null) Color(0xFF1A6DB0) else Color(0xFF212529)
                    )
                },
                onClick = {
                    onSelecionado(null)
                    expanded = false
                }
            )
            opcoes.forEach { opcao ->
                DropdownMenuItem(
                    text = {
                        Text(
                            opcao,
                            fontWeight = if (selecionado == opcao) FontWeight.Bold else FontWeight.Normal,
                            color = if (selecionado == opcao) Color(0xFF1A6DB0) else Color(0xFF212529)
                        )
                    },
                    onClick = {
                        onSelecionado(opcao)
                        expanded = false
                    }
                )
            }
        }
    }
}