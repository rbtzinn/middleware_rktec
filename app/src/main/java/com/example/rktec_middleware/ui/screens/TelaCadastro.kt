package com.example.rktec_middleware.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import com.example.rktec_middleware.ui.components.*
import com.example.rktec_middleware.ui.theme.*
import com.example.rktec_middleware.viewmodel.CadastroState
import com.example.rktec_middleware.viewmodel.CadastroViewModel

@Composable
fun TelaCadastro(
    cadastroViewModel: CadastroViewModel,
    aoCadastroSucesso: () -> Unit,
    aoVoltarLogin: () -> Unit
) {
    val cadastroState by cadastroViewModel.cadastroState.collectAsState()
    var nome by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var senha by remember { mutableStateOf("") }
    var confirmacao by remember { mutableStateOf("") }
    var codigoEmpresa by remember { mutableStateOf("") } // NOVO CAMPO
    var mostrarErro by remember { mutableStateOf<String?>(null) }

    var senhaVisivel by remember { mutableStateOf(false) }
    var confirmacaoVisivel by remember { mutableStateOf(false) }

    LaunchedEffect(cadastroState) {
        when (val state = cadastroState) {
            is CadastroState.Sucesso -> {
                cadastroViewModel.resetar()
                aoCadastroSucesso()
            }
            is CadastroState.Erro -> mostrarErro = state.mensagem
            else -> Unit
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        AuthHeader(
            title = "Crie sua conta",
            subtitle = "Cadastre-se para usar o Middleware"
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Dimens.PaddingLarge)
                .weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // NOVO CAMPO OBRIGATÓRIO PARA O CÓDIGO DA EMPRESA
            StandardTextField(
                value = codigoEmpresa,
                onValueChange = { codigoEmpresa = it.uppercase() }, // Converte para maiúsculas para consistência
                label = "Código da Empresa",
                leadingIcon = { Icon(Icons.Default.Business, contentDescription = "Código da Empresa") }
            )
            Spacer(Modifier.height(Dimens.PaddingMedium))
            StandardTextField(
                value = nome,
                onValueChange = { nome = it },
                label = "Nome completo",
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = "Nome de usuário") }
            )
            Spacer(Modifier.height(Dimens.PaddingMedium))
            StandardTextField(
                value = email,
                onValueChange = { email = it },
                label = "E-mail",
                leadingIcon = { Icon(Icons.Default.MailOutline, contentDescription = "E-mail") }
            )
            Spacer(Modifier.height(Dimens.PaddingMedium))
            StandardTextField(
                value = senha,
                onValueChange = { senha = it },
                label = "Senha",
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = "Senha") },
                visualTransformation = if (senhaVisivel) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { senhaVisivel = !senhaVisivel }) {
                        Icon(
                            imageVector = if (senhaVisivel) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                            contentDescription = if (senhaVisivel) "Ocultar senha" else "Mostrar senha"
                        )
                    }
                }
            )
            Spacer(Modifier.height(Dimens.PaddingMedium))
            StandardTextField(
                value = confirmacao,
                onValueChange = { confirmacao = it },
                label = "Confirme a senha",
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = "Confirme a senha") },
                visualTransformation = if (confirmacaoVisivel) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { confirmacaoVisivel = !confirmacaoVisivel }) {
                        Icon(
                            imageVector = if (confirmacaoVisivel) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                            contentDescription = if (confirmacaoVisivel) "Ocultar senha" else "Mostrar senha"
                        )
                    }
                }
            )
            Spacer(Modifier.height(Dimens.PaddingLarge))

            PrimaryButton(
                onClick = {
                    mostrarErro = null
                    // Validações atualizadas
                    when {
                        codigoEmpresa.isBlank() ->
                            mostrarErro = "O código da empresa é obrigatório!"
                        nome.isBlank() || email.isBlank() || senha.isBlank() || confirmacao.isBlank() ->
                            mostrarErro = "Preencha todos os outros campos!"
                        senha.length < 6 ->
                            mostrarErro = "A senha deve ter pelo menos 6 caracteres."
                        senha != confirmacao ->
                            mostrarErro = "As senhas não conferem!"
                        else -> {
                            val primeiroNome = nome.trim().split(" ").first()
                            // Passa o código da empresa para o ViewModel
                            cadastroViewModel.cadastrar(primeiroNome, email.trim(), senha, codigoEmpresa.trim())
                        }
                    }
                },
                text = "Cadastrar",
                enabled = cadastroState !is CadastroState.Loading
            )

            mostrarErro?.let {
                Text(
                    it,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = Dimens.PaddingSmall)
                )
            }
            Spacer(Modifier.height(Dimens.PaddingMedium))
            SecondaryTextButton(
                onClick = aoVoltarLogin,
                text = "Já tenho uma conta"
            )
        }
    }
}