// ui/screens/TelaGerenciamentoUsuarios.kt
package com.example.rktec_middleware.ui.screens

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.rktec_middleware.data.model.TipoUsuario
import com.example.rktec_middleware.data.model.Usuario
import com.example.rktec_middleware.repository.UsuarioRepository
import com.example.rktec_middleware.ui.components.StandardTextField
import com.example.rktec_middleware.ui.theme.*
import com.example.rktec_middleware.util.LogUtil
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TelaGerenciamentoUsuarios(
    usuarios: List<Usuario>,
    usuarioRepository: UsuarioRepository,
    usuarioLogadoEmail: String,
    context: Context,
    onAtualizarLista: (String) -> Unit,
    onVoltar: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var editandoUsuario by remember { mutableStateOf<Usuario?>(null) }
    var mostrandoDialogoExcluir by remember { mutableStateOf<Usuario?>(null) }

    RKTecMiddlewareTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Gerenciamento de Usuários") },
                    navigationIcon = {
                        IconButton(onClick = onVoltar) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Voltar")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
            },
            containerColor = RktBackground
        ) { padding ->
            LazyColumn(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize(),
                contentPadding = PaddingValues(Dimens.PaddingMedium),
                verticalArrangement = Arrangement.spacedBy(Dimens.PaddingSmall)
            ) {
                items(usuarios, key = { it.email }) { usuario ->
                    UsuarioCard(
                        usuario = usuario,
                        isSelf = usuario.email == usuarioLogadoEmail,
                        onEditClick = { editandoUsuario = it },
                        onDeleteClick = { mostrandoDialogoExcluir = it }
                    )
                }
            }

            if (editandoUsuario != null) {
                DialogEditarUsuario(
                    usuario = editandoUsuario!!,
                    onDismiss = { editandoUsuario = null },
                    onConfirm = { usuarioAtualizado ->
                        scope.launch {
                            usuarioRepository.atualizarUsuario(usuarioAtualizado)
                            LogUtil.logAcaoGerenciamentoUsuario(
                                context = context, usuarioResponsavel = usuarioLogadoEmail, acao = "EDIÇÃO",
                                usuarioAlvo = usuarioAtualizado.email,
                                detalhes = "Dados do usuário ${usuarioAtualizado.email} foram atualizados."
                            )
                            onAtualizarLista(usuarioAtualizado.email)
                            editandoUsuario = null
                        }
                    }
                )
            }

            if (mostrandoDialogoExcluir != null) {
                DialogExcluirUsuario(
                    usuario = mostrandoDialogoExcluir!!,
                    onDismiss = { mostrandoDialogoExcluir = null },
                    onConfirm = { usuario, motivo ->
                        scope.launch {
                            val novaAtividade = !usuario.ativo
                            usuarioRepository.setUsuarioAtivo(usuario.email, novaAtividade)
                            LogUtil.logAcaoGerenciamentoUsuario(
                                context = context, usuarioResponsavel = usuarioLogadoEmail,
                                acao = if (novaAtividade) "REATIVAÇÃO" else "DESATIVAÇÃO",
                                usuarioAlvo = usuario.email, motivo = motivo,
                                detalhes = "Status de ${usuario.email} alterado para ${if (novaAtividade) "ativo" else "inativo"}."
                            )
                            onAtualizarLista(usuario.email)
                            mostrandoDialogoExcluir = null
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun UsuarioCard(
    usuario: Usuario,
    isSelf: Boolean,
    onEditClick: (Usuario) -> Unit,
    onDeleteClick: (Usuario) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = Dimens.PaddingExtraSmall)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.PaddingMedium),
            verticalAlignment = Alignment.CenterVertically
        ) {
            UserAvatar(name = usuario.nome)
            Spacer(modifier = Modifier.width(Dimens.PaddingMedium))
            Column(Modifier.weight(1f)) {
                Text(usuario.nome, style = MaterialTheme.typography.titleLarge)
                Text(usuario.email, style = MaterialTheme.typography.bodyMedium)
                Text(
                    text = if (usuario.tipo == TipoUsuario.ADMIN) "Administrador" else "Membro",
                    color = if (usuario.tipo == TipoUsuario.ADMIN) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            Row {
                IconButton(onClick = { onEditClick(usuario) }) {
                    Icon(Icons.Default.Edit, "Editar", tint = RktTextSecondary)
                }
                if (!isSelf) {
                    IconButton(onClick = { onDeleteClick(usuario) }) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = if (usuario.ativo) "Desativar" else "Reativar",
                            tint = if (usuario.ativo) RktRed else RktGreen
                        )
                    }
                }
            }
        }
    }
}


@Composable
private fun UserAvatar(name: String, modifier: Modifier = Modifier) {
    Box(
        modifier
            .size(48.dp)
            .background(RktBlueLight, shape = CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = name.take(1).uppercase(),
            color = MaterialTheme.colorScheme.primaryContainer,
            style = MaterialTheme.typography.headlineMedium
        )
    }
}

// Adicionar ao final do arquivo TelaGerenciamentoUsuarios.kt

@Composable
private fun DialogEditarUsuario(
    usuario: Usuario,
    onConfirm: (Usuario) -> Unit,
    onDismiss: () -> Unit
) {
    var nomeEditado by remember { mutableStateOf(usuario.nome) }
    var tipoEditado by remember { mutableStateOf(usuario.tipo) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Editar Usuário", style = MaterialTheme.typography.headlineSmall) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(Dimens.PaddingMedium)) {
                StandardTextField(
                    value = nomeEditado,
                    onValueChange = { nomeEditado = it },
                    label = "Nome"
                )
                StandardTextField(
                    value = usuario.email,
                    onValueChange = {},
                    label = "Email",
                    enabled = false
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Dimens.PaddingSmall)
                ) {
                    Text("Tipo:", style = MaterialTheme.typography.bodyLarge)
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
                    val usuarioAtualizado = usuario.copy(nome = nomeEditado, tipo = tipoEditado)
                    onConfirm(usuarioAtualizado)
                },
                enabled = nomeEditado.isNotBlank()
            ) { Text("Salvar") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        },
        shape = MaterialTheme.shapes.large,
        containerColor = MaterialTheme.colorScheme.surface
    )
}

@Composable
private fun DialogExcluirUsuario(
    usuario: Usuario,
    onConfirm: (Usuario, String) -> Unit,
    onDismiss: () -> Unit
) {
    var motivoExclusao by remember { mutableStateOf("") }
    val isAtivo = usuario.ativo

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(if (isAtivo) Icons.Default.Delete else Icons.Default.Restore, contentDescription = null, tint = if (isAtivo) RktRed else RktGreen) },
        title = { Text(if (isAtivo) "Desativar Usuário" else "Reativar Usuário") },
        text = {
            Column {
                Text(if (isAtivo) "Tem certeza que deseja desativar o usuário ${usuario.nome}?" else "Deseja reativar o usuário ${usuario.nome}?")
                if (isAtivo) {
                    Spacer(Modifier.height(Dimens.PaddingMedium))
                    Text("Motivo:", fontWeight = FontWeight.SemiBold)
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(selected = motivoExclusao == "Desligamento de usuário", onClick = { motivoExclusao = "Desligamento de usuário" })
                            Text("Desligamento de usuário")
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(selected = motivoExclusao == "Outros", onClick = { motivoExclusao = "Outros" })
                            Text("Outros")
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(usuario, motivoExclusao) },
                enabled = !isAtivo || motivoExclusao.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = if (isAtivo) RktRed else RktGreen)
            ) {
                Text(if (isAtivo) "Desativar" else "Reativar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        },
        shape = MaterialTheme.shapes.large
    )
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
            shape = MaterialTheme.shapes.small
        ) {
            Text(if (value == TipoUsuario.ADMIN) "Administrador" else "Membro")
            Icon(Icons.Default.ArrowDropDown, contentDescription = null)
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            TipoUsuario.values().forEach { tipo ->
                DropdownMenuItem(
                    text = { Text(if (tipo == TipoUsuario.ADMIN) "Administrador" else "Membro") },
                    onClick = {
                        onValueChange(tipo)
                        expanded = false
                    }
                )
            }
        }
    }
}