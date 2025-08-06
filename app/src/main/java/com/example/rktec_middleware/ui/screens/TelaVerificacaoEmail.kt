package com.example.rktec_middleware.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.rktec_middleware.ui.components.PrimaryButton
import com.example.rktec_middleware.ui.components.SecondaryTextButton
import com.example.rktec_middleware.ui.theme.Dimens
import com.example.rktec_middleware.viewmodel.AuthViewModel
import kotlinx.coroutines.delay

@Composable
fun TelaVerificacaoEmail(
    email: String?,
    onVoltarParaLogin: () -> Unit,
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val context = LocalContext.current

    // ##### LÓGICA DO TIMER #####
    var countdown by remember { mutableStateOf(0) }
    var resendTrigger by remember { mutableStateOf(0) } // Variável para reiniciar o LaunchedEffect

    LaunchedEffect(resendTrigger) {
        if (countdown > 0) {
            for (i in countdown downTo 1) {
                countdown = i
                delay(1000L)
            }
            countdown = 0
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(Dimens.PaddingLarge),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(Icons.Default.Email, contentDescription = "E-mail", modifier = Modifier.size(Dimens.IconSizeLarge * 2), tint = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.height(Dimens.PaddingLarge))
        Text(
            text = "Verifique seu E-mail",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(Dimens.PaddingMedium))
        Text(
            text = "Enviamos um link de confirmação para:\n${email ?: "seu e-mail"}\n\nPor favor, clique no link para ativar sua conta.",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(Dimens.PaddingExtraLarge))

        PrimaryButton(
            onClick = {
                authViewModel.reenviarEmailDeVerificacao()
                Toast.makeText(context, "E-mail de verificação reenviado!", Toast.LENGTH_SHORT).show()
                countdown = 60 // Inicia o timer de 60 segundos
                resendTrigger++ // Dispara o LaunchedEffect novamente
            },
            text = if (countdown > 0) "Reenviar em ${countdown}s" else "Reenviar E-mail",
            enabled = countdown == 0 // Botão só fica ativo quando o timer zera
        )
        Spacer(Modifier.height(Dimens.PaddingSmall))
        SecondaryTextButton(
            onClick = onVoltarParaLogin,
            text = "Já verifiquei. Voltar para o Login"
        )
    }
}