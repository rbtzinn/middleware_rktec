// ui/screens/TelaEsqueciSenha.kt
package com.example.rktec_middleware.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.rktec_middleware.ui.components.PrimaryButton
import com.example.rktec_middleware.ui.components.StandardTextField
import com.example.rktec_middleware.ui.theme.Dimens
import com.example.rktec_middleware.ui.theme.RKTecMiddlewareTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TelaEsqueciSenha(
    aoEnviarCodigo: (String) -> Unit,
    carregando: Boolean = false,
    mensagemErro: String? = null,
    aoVoltarLogin: () -> Unit
) {
    var email by remember { mutableStateOf("") }

    RKTecMiddlewareTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Recuperar Senha") },
                    navigationIcon = {
                        IconButton(onClick = aoVoltarLogin) {
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
            containerColor = MaterialTheme.colorScheme.background
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize()
                    .padding(horizontal = Dimens.PaddingLarge),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Digite o seu e-mail para receber o link de redefinição de senha.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(bottom = Dimens.PaddingLarge)
                )

                StandardTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = "Seu e-mail"
                )

                Spacer(Modifier.height(Dimens.PaddingLarge))

                PrimaryButton(
                    onClick = { aoEnviarCodigo(email.trim()) },
                    text = if (carregando) "Enviando..." else "Enviar código",
                    enabled = email.isNotBlank() && !carregando
                )

                mensagemErro?.let {
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(top = Dimens.PaddingMedium)
                    )
                }
            }
        }
    }
}