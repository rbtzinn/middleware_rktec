package com.example.rktec_middleware.ui.screens

import android.content.Context
import androidx.compose.runtime.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.rktec_middleware.data.model.TipoUsuario
import com.example.rktec_middleware.data.model.Usuario
import com.example.rktec_middleware.repository.UsuarioRepository
import com.example.rktec_middleware.util.LogHelper
import kotlinx.coroutines.launch

private val AzulRktec = Color(0xFF174D86)
private val AzulClaroRktec = Color(0xFF4A90E2)
private val BrancoCard = Color.White
private val AzulSecundario = Color(0xFFDEF1FF)
private val CorTexto = Color(0xFF1B2A3A)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TelaGerenciamentoUsuarios(
    usuarios: List<Usuario>,
    usuarioRepository: UsuarioRepository,
    usuarioLogado: String,
    context: Context,
    onAtualizarLista: () -> Unit,
    onVoltar: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var editandoUsuario by remember { mutableStateOf<Usuario?>(null) }
    var nomeEditado by remember { mutableStateOf("") }
    var tipoEditado by remember { mutableStateOf(TipoUsuario.MEMBRO) }
    var mostrandoDialogoEditar by remember { mutableStateOf(false) }
    var usuarioParaExcluir by remember { mutableStateOf<Usuario?>(null) }
    var mostrandoDialogoExcluir by remember { mutableStateOf(false) }
    var motivoExclusao by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Gerenciamento de Usuários",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onVoltar) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Voltar", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = AzulRktec
                )
            )
        },
        containerColor = AzulSecundario
    ) { padding ->
        Box(
            Modifier
                .padding(padding)
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(AzulClaroRktec.copy(alpha = 0.11f), Color.White),
                        startY = 0f, endY = 1400f
                    )
                )
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 14.dp, bottom = 20.dp),
                contentPadding = PaddingValues(horizontal = 18.dp)
            ) {
                items(usuarios) { usuario ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                            .shadow(4.dp, RoundedCornerShape(18.dp)),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = BrancoCard),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 14.dp, horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Avatar/Inicial
                            Box(
                                Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(AzulClaroRktec.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    usuario.nome.take(1).uppercase(),
                                    color = AzulRktec,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 24.sp
                                )
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            // Info do usuário
                            Column(
                                Modifier.weight(1f)
                            ) {
                                Text(
                                    usuario.nome,
                                    fontWeight = FontWeight.Bold,
                                    color = CorTexto,
                                    fontSize = 18.sp
                                )
                                Text(
                                    usuario.email,
                                    color = Color(0xFF5A6D80),
                                    fontSize = 14.sp
                                )
                                Text(
                                    when (usuario.tipo) {
                                        TipoUsuario.ADMIN -> "Administrador"
                                        TipoUsuario.MEMBRO -> "Membro"
                                    },
                                    color = if (usuario.tipo == TipoUsuario.ADMIN) AzulRktec else Color(0xFF4A90E2),
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 13.sp
                                )
                            }
                            // Botões
                            Row {
                                IconButton(
                                    onClick = {
                                        editandoUsuario = usuario
                                        nomeEditado = usuario.nome
                                        tipoEditado = usuario.tipo
                                        mostrandoDialogoEditar = true
                                    }
                                ) {
                                    Icon(
                                        Icons.Filled.Edit,
                                        contentDescription = "Editar",
                                        tint = AzulRktec
                                    )
                                }
                                IconButton(
                                    onClick = {
                                        usuarioParaExcluir = usuario
                                        motivoExclusao = ""
                                        mostrandoDialogoExcluir = true
                                    }
                                ) {
                                    Icon(
                                        Icons.Filled.Delete,
                                        contentDescription = "Excluir",
                                        tint = Color(0xFFD32F2F)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // -------- Dialog de editar usuário --------
            if (mostrandoDialogoEditar && editandoUsuario != null) {
                AlertDialog(
                    onDismissRequest = { mostrandoDialogoEditar = false },
                    title = { Text("Editar Usuário", fontWeight = FontWeight.Bold) },
                    text = {
                        Column {
                            OutlinedTextField(
                                value = nomeEditado,
                                onValueChange = { nomeEditado = it },
                                label = { Text("Nome") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            OutlinedTextField(
                                value = editandoUsuario!!.email,
                                onValueChange = {},
                                label = { Text("Email") },
                                enabled = false,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 8.dp)
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(top = 12.dp)
                            ) {
                                Text("Tipo:", fontWeight = FontWeight.Medium)
                                Spacer(modifier = Modifier.width(8.dp))
                                DropdownMenuTipoUsuario(
                                    value = tipoEditado,
                                    onValueChange = { tipoEditado = it }
                                )
                            }
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                mostrandoDialogoEditar = false
                                scope.launch {
                                    editandoUsuario?.let {
                                        val usuarioAtualizado = it.copy(
                                            nome = nomeEditado,
                                            tipo = tipoEditado
                                        )
                                        usuarioRepository.atualizarUsuario(usuarioAtualizado)
                                        LogHelper.registrarGerenciamentoUsuario(
                                            context = context,
                                            usuarioResponsavel = usuarioLogado,
                                            acao = "EDIÇÃO",
                                            usuarioAlvo = usuarioAtualizado.email,
                                            motivo = null,
                                            detalhes = "Nome antigo: ${it.nome}, novo: $nomeEditado | Tipo: ${it.tipo.name} → ${tipoEditado.name}"
                                        )
                                        onAtualizarLista()
                                    }
                                }
                            },
                            enabled = nomeEditado.isNotBlank(),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AzulClaroRktec
                            )
                        ) { Text("Salvar edição", color = Color.White) }
                    },
                    dismissButton = {
                        TextButton(onClick = { mostrandoDialogoEditar = false }) {
                            Text("Cancelar", color = AzulRktec)
                        }
                    },
                    shape = RoundedCornerShape(16.dp),
                    containerColor = Color.White
                )
            }

            // -------- Dialog de confirmação de exclusão --------
            if (mostrandoDialogoExcluir && usuarioParaExcluir != null) {
                AlertDialog(
                    onDismissRequest = { mostrandoDialogoExcluir = false },
                    title = { Text("Excluir Usuário", fontWeight = FontWeight.Bold) },
                    text = {
                        Column {
                            Text(
                                "Tem certeza que deseja excluir este usuário?",
                                color = Color(0xFFD32F2F),
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(Modifier.height(14.dp))
                            Text("Motivo da exclusão:", fontWeight = FontWeight.SemiBold)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(
                                    selected = motivoExclusao == "Desligamento de usuário",
                                    onClick = { motivoExclusao = "Desligamento de usuário" },
                                    colors = RadioButtonDefaults.colors(
                                        selectedColor = AzulRktec
                                    )
                                )
                                Text("Desligamento de usuário")
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(
                                    selected = motivoExclusao == "Outros",
                                    onClick = { motivoExclusao = "Outros" },
                                    colors = RadioButtonDefaults.colors(
                                        selectedColor = AzulClaroRktec
                                    )
                                )
                                Text("Outros")
                            }
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                mostrandoDialogoExcluir = false
                                scope.launch {
                                    usuarioParaExcluir?.let {
                                        usuarioRepository.deletarUsuario(it)
                                        LogHelper.registrarGerenciamentoUsuario(
                                            context = context,
                                            usuarioResponsavel = usuarioLogado,
                                            acao = "EXCLUSÃO",
                                            usuarioAlvo = it.email,
                                            motivo = motivoExclusao,
                                            detalhes = "Usuário excluído: ${it.nome} (${it.email})"
                                        )
                                        onAtualizarLista()
                                    }
                                    motivoExclusao = ""
                                }
                            },
                            enabled = motivoExclusao.isNotBlank(),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFD32F2F)
                            )
                        ) { Text("Excluir", color = Color.White) }
                    },
                    dismissButton = {
                        TextButton(onClick = { mostrandoDialogoExcluir = false }) {
                            Text("Cancelar", color = AzulRktec)
                        }
                    },
                    shape = RoundedCornerShape(16.dp),
                    containerColor = Color.White
                )
            }
        }
    }
}

@Composable
fun DropdownMenuTipoUsuario(
    value: TipoUsuario,
    onValueChange: (TipoUsuario) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        OutlinedButton(
            onClick = { expanded = true },
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = AzulClaroRktec
            )
        ) {
            Text(
                when (value) {
                    TipoUsuario.ADMIN -> "Administrador"
                    TipoUsuario.MEMBRO -> "Membro"
                },
                fontWeight = FontWeight.SemiBold
            )
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(Color.White)
        ) {
            TipoUsuario.values().forEach { tipo ->
                DropdownMenuItem(
                    text = {
                        Text(
                            when (tipo) {
                                TipoUsuario.ADMIN -> "Administrador"
                                TipoUsuario.MEMBRO -> "Membro"
                            }
                        )
                    },
                    onClick = {
                        onValueChange(tipo)
                        expanded = false
                    }
                )
            }
        }
    }
}
