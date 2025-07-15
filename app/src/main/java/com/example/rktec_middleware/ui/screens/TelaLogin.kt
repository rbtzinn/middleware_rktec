// ui/screens/TelaLogin.kt
package com.example.rktec_middleware.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.example.rktec_middleware.data.db.AppDatabase
import com.example.rktec_middleware.data.model.Usuario
import com.example.rktec_middleware.ui.components.*
import com.example.rktec_middleware.ui.theme.*
import com.example.rktec_middleware.viewmodel.LoginState
import com.example.rktec_middleware.viewmodel.LoginViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun TelaLogin(
    onLoginSucesso: (Usuario) -> Unit,
    onEsqueciSenha: (String) -> Unit,
    onIrParaCadastro: () -> Unit,
    viewModel: LoginViewModel
) {
    val loginState by viewModel.loginState.collectAsState()
    var usuario by remember { mutableStateOf("") }
    var senha by remember { mutableStateOf("") }
    var senhaVisivel by remember { mutableStateOf(false) }
    var showSuggestions by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val appDatabase = remember { AppDatabase.getInstance(context) }
    var emailsCadastrados by remember { mutableStateOf<List<String>>(emptyList()) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        viewModel.resetarEstado()
        scope.launch(Dispatchers.IO) {
            emailsCadastrados = appDatabase.usuarioDao().listarEmails()
        }
    }

    val filteredSuggestions = emailsCadastrados.filter {
        it.contains(usuario, ignoreCase = true) && usuario.isNotBlank()
    }.take(6)

    var erroMsg by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(loginState) {
        when (val state = loginState) {
            is LoginState.Sucesso -> onLoginSucesso(state.usuario)
            is LoginState.Erro -> erroMsg = state.mensagem
            else -> erroMsg = null
        }
    }

    RKTecMiddlewareTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            AuthHeader(
                title = "Bem-vindo ao Middleware",
                subtitle = "Faça login para continuar"
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = Dimens.PaddingLarge),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // AUTOCOMPLETE
                Box {
                    StandardTextField(
                        value = usuario,
                        onValueChange = {
                            usuario = it
                            showSuggestions = it.isNotBlank()
                        },
                        label = "E-mail",
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = "E-mail") }
                    )
                    if (showSuggestions && filteredSuggestions.isNotEmpty()) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 60.dp), // Ajuste conforme necessário
                            shape = MaterialTheme.shapes.medium,
                            elevation = CardDefaults.cardElevation(defaultElevation = Dimens.PaddingSmall)
                        ) {
                            Column(modifier = Modifier.background(MaterialTheme.colorScheme.surface)) {
                                filteredSuggestions.forEach { suggestion ->
                                    Text(
                                        text = suggestion,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                usuario = suggestion
                                                showSuggestions = false
                                            }
                                            .padding(Dimens.PaddingMedium),
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = RktTextPrimary
                                    )
                                }
                            }
                        }
                    }
                }

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

                Spacer(Modifier.height(Dimens.PaddingSmall))

                TextButton(
                    onClick = { onEsqueciSenha(usuario) },
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Esqueci minha senha", color = RktTextSecondary)
                }

                Spacer(Modifier.height(Dimens.PaddingLarge))

                PrimaryButton(
                    onClick = { viewModel.autenticar(usuario.trim(), senha) },
                    text = "Entrar",
                    enabled = usuario.isNotBlank() && senha.isNotBlank() && loginState !is LoginState.Loading
                )

                erroMsg?.let {
                    Text(
                        it,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(top = Dimens.PaddingMedium)
                    )
                }

                Spacer(Modifier.height(Dimens.PaddingMedium))

                SecondaryTextButton(
                    onClick = onIrParaCadastro,
                    text = "Criar nova conta"
                )
            }
        }
    }
}