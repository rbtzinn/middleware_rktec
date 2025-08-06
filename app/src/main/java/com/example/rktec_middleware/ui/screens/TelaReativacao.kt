package com.example.rktec_middleware.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LockReset
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.rktec_middleware.ui.components.PrimaryButton
import com.example.rktec_middleware.ui.components.StandardTextField
import com.example.rktec_middleware.ui.theme.Dimens
import com.example.rktec_middleware.viewmodel.AuthViewModel
import com.example.rktec_middleware.viewmodel.ReativacaoState

@Composable
fun TelaReativacao(
    authViewModel: AuthViewModel, // Recebe o AuthViewModel para controlar o estado
    onLogoutClick: () -> Unit
) {
    val reativacaoState by authViewModel.reativacaoState.collectAsState()
    var codigoConvite by remember { mutableStateOf("") }
    val context = LocalContext.current

    LaunchedEffect(reativacaoState) {
        if (reativacaoState is ReativacaoState.Error) {
            Toast.makeText(context, (reativacaoState as ReativacaoState.Error).message, Toast.LENGTH_LONG).show()
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(Dimens.PaddingLarge),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Default.LockReset, "Conta Inativa", modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.height(Dimens.PaddingLarge))
        Text("Conta Inativa", style = MaterialTheme.typography.headlineSmall, textAlign = TextAlign.Center)
        Text(
            "Sua conta foi desativada. Para reativá-la e ingressar em uma nova empresa, por favor, insira o código de convite abaixo.",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(vertical = Dimens.PaddingMedium)
        )
        StandardTextField(
            value = codigoConvite,
            onValueChange = { codigoConvite = it },
            label = "Código da Nova Empresa"
        )
        Spacer(Modifier.height(Dimens.PaddingLarge))
        PrimaryButton(
            onClick = { authViewModel.reativarComNovoCodigo(codigoConvite) },
            text = "Reativar Conta",
            isLoading = reativacaoState is ReativacaoState.Loading,
            enabled = codigoConvite.isNotBlank() && reativacaoState !is ReativacaoState.Loading
        )
        Spacer(Modifier.height(Dimens.PaddingSmall))
        TextButton(onClick = onLogoutClick) {
            Text("Sair")
        }
    }
}