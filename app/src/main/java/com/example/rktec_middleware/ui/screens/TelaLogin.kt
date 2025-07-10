package com.example.rktec_middleware.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.rktec_middleware.data.db.AppDatabase
import com.example.rktec_middleware.data.model.Usuario
import com.example.rktec_middleware.viewmodel.LoginState
import com.example.rktec_middleware.viewmodel.LoginViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
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
    var mostrarErro by remember { mutableStateOf(false) }
    var erroMsg by remember { mutableStateOf("") }
    var senhaVisivel by remember { mutableStateOf(false) }
    var showSuggestions by remember { mutableStateOf(false) }

    // Pega os e-mails do banco REAL usando Room (AppDatabase)
    val context = LocalContext.current
    val appDatabase = remember { AppDatabase.getInstance(context) }
    var emailsCadastrados by remember { mutableStateOf<List<String>>(emptyList()) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        viewModel.resetarEstado()
    }

    LaunchedEffect(Unit) {
        // Evite travar a UI: rode no IO dispatcher
        scope.launch(Dispatchers.IO) {
            emailsCadastrados = appDatabase.usuarioDao().listarEmails()
        }
    }

    // Filtra sugestões pelo texto digitado
    val filteredSuggestions = emailsCadastrados.filter {
        it.contains(usuario, ignoreCase = true) && usuario.isNotBlank()
    }.take(6)

    LaunchedEffect(loginState) {
        when (loginState) {
            is LoginState.Sucesso -> {
                onLoginSucesso((loginState as LoginState.Sucesso).usuario)
            }
            is LoginState.Erro -> {
                mostrarErro = true
                erroMsg = (loginState as LoginState.Erro).mensagem
            }
            else -> Unit
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F7FA))
    ) {
        // Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .background(
                    Brush.verticalGradient(
                        0f to Color(0xFF4A90E2),
                        1f to Color(0xFF174D86)
                    )
                )
                .shadow(3.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .background(Color.White, CircleShape)
                        .shadow(4.dp, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "RKT",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF4A90E2),
                        letterSpacing = 2.sp
                    )
                }
                Spacer(Modifier.height(16.dp))
                Text(
                    "Bem-vindo ao Middleware",
                    fontSize = 21.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "Faça login para continuar",
                    fontSize = 15.sp,
                    color = Color.White.copy(alpha = 0.85f)
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(Modifier.height(18.dp))

            // AUTOCOMPLETE
            Box {
                OutlinedTextField(
                    value = usuario,
                    onValueChange = {
                        usuario = it
                        showSuggestions = it.isNotBlank()
                    },
                    label = { Text("E-mail") },
                    singleLine = true,
                    leadingIcon = {
                        Icon(Icons.Filled.Person, contentDescription = "Usuário", tint = Color(0xFF4A90E2))
                    },
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                // Sugestões aparecendo embaixo sem fechar o teclado
                if (showSuggestions && filteredSuggestions.isNotEmpty()) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 56.dp) // pra aparecer abaixo do campo
                            .shadow(8.dp, RoundedCornerShape(14.dp))
                    ) {
                        Column(modifier = Modifier.background(Color.White)) {
                            filteredSuggestions.forEach { suggestion ->
                                Text(
                                    text = suggestion,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            usuario = suggestion
                                            showSuggestions = false
                                        }
                                        .padding(horizontal = 16.dp, vertical = 12.dp),
                                    fontSize = 16.sp,
                                    color = Color(0xFF174D86)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(14.dp))

            OutlinedTextField(
                value = senha,
                onValueChange = { senha = it },
                label = { Text("Senha") },
                singleLine = true,
                visualTransformation = if (senhaVisivel) VisualTransformation.None else PasswordVisualTransformation(),
                leadingIcon = {
                    Icon(Icons.Filled.Lock, contentDescription = "Senha", tint = Color(0xFF4A90E2))
                },
                trailingIcon = {
                    IconButton(
                        onClick = { senhaVisivel = !senhaVisivel }
                    ) {
                        Icon(
                            imageVector = if (senhaVisivel) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                            contentDescription = if (senhaVisivel) "Ocultar senha" else "Mostrar senha"
                        )
                    }
                },
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(8.dp))

            TextButton(
                onClick = { onEsqueciSenha(usuario) },
                modifier = Modifier.align(Alignment.End)
            ) {
                Text("Esqueci minha senha", color = Color(0xFF174D86), fontSize = 13.sp)
            }

            Spacer(Modifier.height(20.dp))

            Button(
                onClick = { viewModel.autenticar(usuario.trim(), senha) },
                enabled = usuario.isNotBlank() && senha.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(22.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4A90E2))
            ) {
                Text("Entrar", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }

            if (mostrarErro) {
                Text(
                    erroMsg,
                    color = Color.Red,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(top = 10.dp)
                )
            }

            Spacer(Modifier.height(18.dp))

            TextButton(
                onClick = onIrParaCadastro,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text(
                    "Criar nova conta",
                    color = Color(0xFF4A90E2),
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        }
    }
}
