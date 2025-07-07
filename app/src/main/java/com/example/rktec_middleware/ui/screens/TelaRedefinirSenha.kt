package com.example.rktec_middleware.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun TelaRedefinirSenha(
    aoSalvar: (String) -> Unit,
    carregando: Boolean = false,
    mensagemErro: String? = null
) {
    var novaSenha by remember { mutableStateOf("") }
    var confirmacao by remember { mutableStateOf("") }

    Column(
        Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Nova Senha", fontWeight = FontWeight.Bold, fontSize = 20.sp)
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = novaSenha,
            onValueChange = { novaSenha = it },
            label = { Text("Digite a nova senha") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation()
        )
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = confirmacao,
            onValueChange = { confirmacao = it },
            label = { Text("Confirme a nova senha") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation()
        )
        Spacer(Modifier.height(16.dp))
        Button(
            onClick = { aoSalvar(novaSenha) },
            enabled = novaSenha.isNotBlank() && novaSenha == confirmacao && !carregando,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (carregando) "Salvando..." else "Salvar nova senha")
        }
        mensagemErro?.let {
            Text(it, color = Color.Red, modifier = Modifier.padding(top = 8.dp))
        }
    }
}
