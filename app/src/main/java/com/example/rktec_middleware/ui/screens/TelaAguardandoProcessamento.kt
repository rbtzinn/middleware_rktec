package com.example.rktec_middleware.ui.screens

import android.app.Activity
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.rktec_middleware.ui.components.PrimaryButton
import com.example.rktec_middleware.viewmodel.ProcessamentoViewModel
import com.example.rktec_middleware.viewmodel.StatusProcessamento

@Composable
fun TelaAguardandoProcessamento(
    companyId: String,
    onProcessamentoConcluido: () -> Unit,
    viewModel: ProcessamentoViewModel = hiltViewModel()
) {
    val status by viewModel.status.collectAsState()
    val context = LocalContext.current

    // Mantém a tela sempre ligada durante o processo
    val keepScreenOn = status is StatusProcessamento.Processando || status is StatusProcessamento.Baixando
    DisposableEffect(keepScreenOn) {
        val activity = context as? Activity
        val window = activity?.window
        if (keepScreenOn) {
            window?.addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        } else {
            window?.clearFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
        onDispose {
            window?.clearFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    LaunchedEffect(companyId) {
        viewModel.escutarStatusProcessamento(companyId)
    }

    LaunchedEffect(status) {
        if (status is StatusProcessamento.Concluido) {
            onProcessamentoConcluido()
        }
    }

    // Usando Crossfade para uma transição suave entre os estados
    Crossfade(targetState = status, label = "statusAnimation") { state ->
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            when (state) {
                is StatusProcessamento.Processando -> {
                    InfoConteudo(
                        titulo = "Processando sua planilha...",
                        mensagem = "Seu inventário está sendo preparado na nuvem. Por favor, aguarde.",
                        isLoading = true
                    )
                }
                is StatusProcessamento.ProntoParaBaixar -> {
                    InfoConteudo(
                        titulo = "Planilha Pronta!",
                        mensagem = "A base de dados da sua empresa foi processada e está pronta para ser instalada neste dispositivo.",
                        isLoading = false,
                        botaoTexto = "Sincronizar Dados Agora",
                        onBotaoClick = { viewModel.iniciarSincronizacaoLocal() },
                        icon = { Icon(Icons.Default.CloudDone, contentDescription = "Pronto", modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.primary) }
                    )
                }
                is StatusProcessamento.Baixando -> {
                    InfoConteudo(
                        titulo = "Sincronizando...",
                        mensagem = "Baixando e instalando a base de dados no seu dispositivo. Quase lá!",
                        isLoading = true
                    )
                }
                is StatusProcessamento.Erro -> {
                    InfoConteudo(
                        titulo = "Ocorreu um Erro",
                        mensagem = state.mensagem,
                        isLoading = false,
                        icon = { Icon(Icons.Default.Error, contentDescription = "Erro", modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.error) }
                    )
                }
                else -> { // Ocioso e Concluido (que navega rapidamente)
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoConteudo(
    titulo: String,
    mensagem: String,
    isLoading: Boolean,
    botaoTexto: String? = null,
    onBotaoClick: (() -> Unit)? = null,
    icon: @Composable () -> Unit = { CircularProgressIndicator(modifier = Modifier.size(64.dp)) }
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.padding(horizontal = 32.dp)
    ) {
        Text(titulo, style = MaterialTheme.typography.headlineSmall, textAlign = TextAlign.Center)

        if (isLoading) {
            icon()
        } else {
            Spacer(modifier = Modifier.height(16.dp))
            icon()
            Spacer(modifier = Modifier.height(16.dp))
        }

        Text(
            text = mensagem,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )

        if (botaoTexto != null && onBotaoClick != null) {
            Spacer(modifier = Modifier.height(16.dp))
            PrimaryButton(onClick = onBotaoClick, text = botaoTexto)
        }
    }
}