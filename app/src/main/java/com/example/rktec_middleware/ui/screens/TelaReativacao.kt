package com.example.rktec_middleware.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import com.example.rktec_middleware.ui.components.AuthHeader
import com.example.rktec_middleware.ui.components.PrimaryButton
import com.example.rktec_middleware.ui.components.StandardTextField
import com.example.rktec_middleware.ui.theme.Dimens
import com.example.rktec_middleware.viewmodel.LoginState
import com.example.rktec_middleware.viewmodel.LoginViewModel

@Composable
fun TelaReativacao(
    viewModel: LoginViewModel,
    onCancel: () -> Unit
) {
    val state by viewModel.loginState.collectAsState()
    var novoCodigoEmpresa by remember { mutableStateOf("") }

    val usuarioInativo = (state as? LoginState.Inativo)?.usuario
    val context = LocalContext.current

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AuthHeader(title = "Reativar Conta", subtitle = "Bem-vindo de volta, ${usuarioInativo?.nome ?: ""}")

        Column(
            modifier = Modifier.fillMaxSize().padding(Dimens.PaddingLarge),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                "Sua conta está inativa. Para reativá-la e entrar em uma nova empresa, por favor, insira o código da empresa abaixo.",
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyLarge
            )
            Spacer(Modifier.height(Dimens.PaddingLarge))

            StandardTextField(
                value = novoCodigoEmpresa,
                onValueChange = { novoCodigoEmpresa = it.uppercase() },
                label = "Código da Nova Empresa"
            )
            Spacer(Modifier.height(Dimens.PaddingLarge))

            val isLoading = state is LoginState.Loading
            PrimaryButton(
                onClick = { viewModel.reativarETransferir(novoCodigoEmpresa) },
                text = if (isLoading) "Verificando..." else "Reativar e Entrar",
                enabled = novoCodigoEmpresa.isNotBlank() && !isLoading
            )

            val currentState = state
            if (currentState is LoginState.Erro) {
                Text(
                    text = currentState.mensagem,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = Dimens.PaddingMedium)
                )
            }

            TextButton(onClick = {
                viewModel.resetarEstado()
                onCancel()
            }) {
                Text("Cancelar e Voltar")
            }
        }
    }
}