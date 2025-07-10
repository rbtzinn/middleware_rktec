package com.example.rktec_middleware.ui.components

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.rktec_middleware.data.model.TipoUsuario
import com.example.rktec_middleware.data.model.Usuario
import com.example.rktec_middleware.data.dao.UsuarioDao
import kotlinx.coroutines.launch

@Composable
fun TelaPromoverAdminDialog(
    usuarioLogado: Usuario,
    usuarioDao: UsuarioDao,
    onPromovido: () -> Unit,
    onCancelar: () -> Unit
) {
    var codigo by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onCancelar,
        title = { Text("Ativar Modo Administrador") },
        text = {
            Column {
                Text("Digite o código secreto para ativar modo ADMIN.")
                OutlinedTextField(
                    value = codigo,
                    onValueChange = { codigo = it },
                    label = { Text("Código Secreto") }
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                if (codigo == "@DM2025") {
                    scope.launch {
                        usuarioDao.atualizar(usuarioLogado.copy(tipo = TipoUsuario.ADMIN))
                        onPromovido()
                    }
                } else {
                    Toast.makeText(context, "Código incorreto", Toast.LENGTH_SHORT).show()
                }
            }) { Text("Ativar") }
        },
        dismissButton = {
            TextButton(onClick = onCancelar) { Text("Cancelar") }
        }
    )
}
