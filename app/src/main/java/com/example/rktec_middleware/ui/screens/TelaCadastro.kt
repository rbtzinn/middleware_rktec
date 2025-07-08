package com.example.rktec_middleware.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MailOutline
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.rktec_middleware.viewmodel.CadastroState
import com.example.rktec_middleware.viewmodel.CadastroViewModel

@OptIn(ExperimentalMaterial3Api::class)
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
    var mostrarErro by remember { mutableStateOf<String?>(null) }

    var senhaVisivel by remember { mutableStateOf(false) }
    var confirmacaoVisivel by remember { mutableStateOf(false) }

    LaunchedEffect(cadastroState) {
        when (cadastroState) {
            is CadastroState.Sucesso -> {
                cadastroViewModel.resetar()
                aoCadastroSucesso()
            }
            is CadastroState.Erro -> {
                mostrarErro = (cadastroState as CadastroState.Erro).mensagem
            }
            else -> Unit
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F7FA))
    ) {
        // Header gradiente vertical
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
                    "Crie sua conta",
                    fontSize = 21.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "Cadastre-se para usar o Middleware",
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
            OutlinedTextField(
                value = nome,
                onValueChange = { nome = it },
                label = { Text("Nome de usuário") },
                singleLine = true,
                leadingIcon = {
                    Icon(Icons.Filled.Person, contentDescription = "Usuário", tint = Color(0xFF4A90E2))
                },
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(14.dp))
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("E-mail") },
                singleLine = true,
                leadingIcon = {
                    Icon(Icons.Filled.MailOutline, contentDescription = "E-mail", tint = Color(0xFF4A90E2))
                },
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(14.dp))
            OutlinedTextField(
                value = senha,
                onValueChange = { senha = it },
                label = { Text("Senha") },
                singleLine = true,
                leadingIcon = {
                    Icon(Icons.Filled.Lock, contentDescription = "Senha", tint = Color(0xFF4A90E2))
                },
                shape = RoundedCornerShape(16.dp),
                visualTransformation = if (senhaVisivel) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { senhaVisivel = !senhaVisivel }) {
                        Icon(
                            imageVector = if (senhaVisivel) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                            contentDescription = if (senhaVisivel) "Ocultar senha" else "Mostrar senha"
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(14.dp))
            OutlinedTextField(
                value = confirmacao,
                onValueChange = { confirmacao = it },
                label = { Text("Confirme a senha") },
                singleLine = true,
                leadingIcon = {
                    Icon(Icons.Filled.Lock, contentDescription = "Confirme a senha", tint = Color(0xFF4A90E2))
                },
                shape = RoundedCornerShape(16.dp),
                visualTransformation = if (confirmacaoVisivel) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { confirmacaoVisivel = !confirmacaoVisivel }) {
                        Icon(
                            imageVector = if (confirmacaoVisivel) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                            contentDescription = if (confirmacaoVisivel) "Ocultar senha" else "Mostrar senha"
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(22.dp))
            Button(
                onClick = {
                    mostrarErro = null
                    when {
                        nome.isBlank() || email.isBlank() || senha.isBlank() || confirmacao.isBlank() -> {
                            mostrarErro = "Preencha todos os campos!"
                        }
                        senha.length < 6 -> {
                            mostrarErro = "A senha deve ter pelo menos 6 caracteres."
                        }
                        !senha.any { it.isDigit() } -> {
                            mostrarErro = "A senha deve conter pelo menos um número."
                        }
                        !senha.any { it.isLetter() } -> {
                            mostrarErro = "A senha deve conter pelo menos uma letra."
                        }
                        senha != confirmacao -> {
                            mostrarErro = "As senhas não conferem!"
                        }
                        else -> {
                            cadastroViewModel.cadastrar(nome.trim(), email.trim(), senha)
                        }
                    }
                },
                enabled = cadastroState !is CadastroState.Loading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(22.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4A90E2))
            ) {
                Text("Cadastrar", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }

            mostrarErro?.let {
                Text(it, color = Color.Red, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(top = 8.dp))
            }
            Spacer(Modifier.height(18.dp))
            TextButton(
                onClick = aoVoltarLogin,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text(
                    "Já tenho uma conta",
                    color = Color(0xFF4A90E2),
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        }
    }
}
