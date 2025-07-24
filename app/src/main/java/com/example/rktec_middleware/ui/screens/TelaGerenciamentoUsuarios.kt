// ui/screens/TelaGerenciamentoUsuarios.kt
package com.example.rktec_middleware.ui.screens

import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.rktec_middleware.data.model.TipoUsuario
import com.example.rktec_middleware.data.model.Usuario
import com.example.rktec_middleware.repository.UsuarioRepository
import com.example.rktec_middleware.ui.components.GradientHeader
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.rktec_middleware.viewmodel.GerenciamentoViewModel
import com.example.rktec_middleware.ui.components.StandardTextField
import com.example.rktec_middleware.ui.theme.*
import com.example.rktec_middleware.util.LogUtil
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TelaGerenciamentoUsuarios(
    viewModel: GerenciamentoViewModel = hiltViewModel(),
    onVoltar: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val usuarios by viewModel.usuarios.collectAsState()
    val usuarioLogadoEmail = viewModel.usuarioLogadoEmail

    var editandoUsuario by remember { mutableStateOf<Usuario?>(null) }
    var mostrandoDialogoExcluir by remember { mutableStateOf<Usuario?>(null) }


        Scaffold(
            topBar = {
                GradientHeader(title = "Gerenciamento de Usuários", onVoltar = onVoltar)
            },
            containerColor = MaterialTheme.colorScheme.background
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
                        viewModel.atualizarUsuario(usuarioAtualizado)
                        editandoUsuario = null
                    }
                )
            }

            if (mostrandoDialogoExcluir != null) {
                DialogExcluirUsuario(
                    usuario = mostrandoDialogoExcluir!!,
                    onDismiss = { mostrandoDialogoExcluir = null },
                    onConfirm = { usuario, motivo ->
                        viewModel.alternarAtividadeUsuario(usuario, motivo)
                        mostrandoDialogoExcluir = null
                    }
                )
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
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            // Deixa o card mais escuro se o usuário estiver inativo
            disabledContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (usuario.ativo) Dimens.PaddingExtraSmall else 0.dp)
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
                    color = if (usuario.tipo == TipoUsuario.ADMIN) RktOrange else MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            Row {
                IconButton(onClick = { onEditClick(usuario) }, enabled = usuario.ativo) {
                    Icon(Icons.Default.Edit, "Editar", tint = if(usuario.ativo) RktTextSecondary else RktTextSecondary.copy(alpha = 0.5f))
                }
                if (!isSelf) {
                    IconButton(onClick = { onDeleteClick(usuario) }) {
                        Icon(
                            imageVector = if (usuario.ativo) Icons.Default.Delete else Icons.Default.Restore,
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
            color = MaterialTheme.colorScheme.primary,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

// -----------------------------------------------------------------------------------
// ALTERAÇÃO: Todos os diálogos abaixo foram refeitos com um estilo mais moderno e legível.
// -----------------------------------------------------------------------------------

@OptIn(ExperimentalMaterial3Api::class)
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
        icon = { Icon(Icons.Default.Edit, contentDescription = null) },
        title = { Text("Editar Usuário") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(Dimens.PaddingMedium)) {
                // Campo de nome
                StandardTextField(
                    value = nomeEditado,
                    onValueChange = { nomeEditado = it },
                    label = "Nome"
                )

                // Campo de e-mail (não editável)
                StandardTextField(
                    value = usuario.email,
                    onValueChange = {},
                    label = "Email",
                    enabled = false
                )

                // Dropdown para tipo de usuário (com novo visual)
                DropdownMenuTipoUsuario(
                    label = "Tipo de Usuário",
                    value = tipoEditado,
                    onValueChange = { tipoEditado = it }
                )
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
        shape = MaterialTheme.shapes.large
    )
}

@Composable
private fun DialogExcluirUsuario(
    usuario: Usuario,
    onConfirm: (Usuario, String) -> Unit,
    onDismiss: () -> Unit
) {
    val isAtivo = usuario.ativo
    val (selectedOption, onOptionSelected) = remember { mutableStateOf("") }
    var outroMotivo by remember { mutableStateOf("") }

    val motivoFinal = if (selectedOption == "Outros") outroMotivo else selectedOption

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(if (isAtivo) Icons.Default.Delete else Icons.Default.Restore, contentDescription = null, tint = if (isAtivo) RktRed else RktGreen) },
        title = { Text(if (isAtivo) "Desativar Usuário" else "Reativar Usuário") },
        text = {
            Column {
                Text(if (isAtivo) "Tem certeza que deseja desativar o usuário ${usuario.nome}?" else "Deseja reativar o usuário ${usuario.nome}?")
                if (isAtivo) {
                    Spacer(Modifier.height(Dimens.PaddingMedium))
                    Text("Motivo:", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.titleMedium)

                    // Radio buttons mais interativos
                    Column(Modifier.selectableGroup()) {
                        SelectableRow("Desligamento de usuário", selectedOption, onOptionSelected)
                        SelectableRow("Outros", selectedOption, onOptionSelected)
                    }

                    // Campo de texto que aparece se "Outros" for selecionado
                    AnimatedVisibility(visible = selectedOption == "Outros") {
                        StandardTextField(
                            value = outroMotivo,
                            onValueChange = { outroMotivo = it },
                            label = "Especifique o motivo",
                            modifier = Modifier.padding(top = Dimens.PaddingSmall)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(usuario, motivoFinal) },
                enabled = !isAtivo || motivoFinal.isNotBlank(),
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
private fun SelectableRow(
    text: String,
    selectedOption: String,
    onOptionSelected: (String) -> Unit
) {
    Row(
        Modifier
            .fillMaxWidth()
            .height(48.dp)
            .selectable(
                selected = (text == selectedOption),
                onClick = { onOptionSelected(text) },
                role = Role.RadioButton
            )
            .padding(horizontal = Dimens.PaddingSmall),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = (text == selectedOption),
            onClick = null // O clique é controlado pela Row
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(start = Dimens.PaddingSmall)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DropdownMenuTipoUsuario(
    label: String,
    value: TipoUsuario,
    onValueChange: (TipoUsuario) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val displayText = if (value == TipoUsuario.ADMIN) "Administrador" else "Membro"

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
    ) {
        OutlinedTextField(
            value = displayText,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth(),
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            shape = MaterialTheme.shapes.medium,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = RktStroke
            )
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(MaterialTheme.colorScheme.surface)
        ) {
            TipoUsuario.values().forEach { tipo ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = if (tipo == TipoUsuario.ADMIN) "Administrador" else "Membro",
                            fontWeight = if (tipo == value) FontWeight.Bold else FontWeight.Normal,
                            color = MaterialTheme.colorScheme.onSurface
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