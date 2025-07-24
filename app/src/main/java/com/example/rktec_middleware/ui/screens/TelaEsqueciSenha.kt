package com.example.rktec_middleware.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.rktec_middleware.ui.components.PrimaryButton
import com.example.rktec_middleware.ui.components.StandardTextField
import com.example.rktec_middleware.ui.theme.Dimens
import com.example.rktec_middleware.viewmodel.RecuperarSenhaState
import com.example.rktec_middleware.viewmodel.RecuperarSenhaViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TelaEsqueciSenha(
    aoVoltarLogin: () -> Unit,
    viewModel: RecuperarSenhaViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    var email by remember { mutableStateOf(viewModel.emailInicial ?: "") }
    val context = LocalContext.current

    LaunchedEffect(state) {
        if (state is RecuperarSenhaState.Success) {
            Toast.makeText(context, "Link de recuperação enviado para o seu e-mail!", Toast.LENGTH_LONG).show()
            viewModel.resetState()
            aoVoltarLogin()
        }
    }

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
                modifier = Modifier.padding(bottom = Dimens.PaddingLarge)
            )

            StandardTextField(
                value = email,
                onValueChange = { email = it },
                label = "Seu e-mail"
            )

            Spacer(Modifier.height(Dimens.PaddingLarge))

            val isLoading = state is RecuperarSenhaState.Loading
            PrimaryButton(
                onClick = { viewModel.enviarResetEmail(email.trim()) },
                text = if (isLoading) "Enviando..." else "Enviar Link",
                enabled = email.isNotBlank() && !isLoading
            )

            // CORREÇÃO: Criar uma variável local para o smart cast funcionar.
            val currentState = state
            if (currentState is RecuperarSenhaState.Error) {
                Text(
                    text = currentState.message,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = Dimens.PaddingMedium)
                )
            }
        }
    }
}