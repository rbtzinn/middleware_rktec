package com.example.rktec_middleware.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    var mostrarErro by remember { mutableStateOf<String?>(null) }

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
            .background(Color.White)
            .padding(top = 32.dp, bottom = 24.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .offset(y = -32.dp)
                .background(
                    Brush.horizontalGradient(
                        listOf(Color(0xFF4A90E2), Color(0xFF174D86))
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "Cadastro de Usuário",
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 32.dp)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            OutlinedTextField(
                value = nome,
                onValueChange = { nome = it },
                label = { Text("Nome de usuário") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(14.dp))
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("E-mail") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(14.dp))
            OutlinedTextField(
                value = senha,
                onValueChange = { senha = it },
                label = { Text("Senha") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = PasswordVisualTransformation()
            )
            Spacer(Modifier.height(14.dp))
            OutlinedTextField(
                value = confirmacao,
                onValueChange = { confirmacao = it },
                label = { Text("Confirme a senha") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = PasswordVisualTransformation()
            )
            Spacer(Modifier.height(22.dp))
            Button(
                onClick = {
                    mostrarErro = null
                    if (nome.isBlank() || email.isBlank() || senha.isBlank() || confirmacao.isBlank()) {
                        mostrarErro = "Preencha todos os campos!"
                    } else if (senha != confirmacao) {
                        mostrarErro = "As senhas não conferem!"
                    } else {
                        cadastroViewModel.cadastrar(nome.trim(), email.trim(), senha)
                    }
                },
                enabled = cadastroState !is CadastroState.Loading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4A90E2))
            ) {
                Text("Cadastrar", fontSize = 20.sp, color = Color.White)
            }
            mostrarErro?.let {
                Text(it, color = Color.Red, modifier = Modifier.padding(top = 8.dp))
            }
            Spacer(Modifier.height(14.dp))
            TextButton(
                onClick = aoVoltarLogin
            ) {
                Text("Já tenho uma conta", color = Color(0xFF174D86))
            }
        }
    }
}
