package com.example.rktec_middleware.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun TelaCodigoVerificacao(
    aoValidarCodigo: (String) -> Unit,
    carregando: Boolean = false,
    mensagemErro: String? = null
) {
    var codigo by remember { mutableStateOf("") }

    Column(
        Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Digite o código enviado ao seu e-mail", fontWeight = FontWeight.Bold, fontSize = 20.sp)
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = codigo,
            onValueChange = { codigo = it },
            label = { Text("Código de 6 dígitos") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(16.dp))
        Button(
            onClick = { aoValidarCodigo(codigo.trim()) },
            enabled = codigo.length == 6 && !carregando,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (carregando) "Validando..." else "Validar código")
        }
        mensagemErro?.let {
            Text(it, color = Color.Red, modifier = Modifier.padding(top = 8.dp))
        }
    }
}
