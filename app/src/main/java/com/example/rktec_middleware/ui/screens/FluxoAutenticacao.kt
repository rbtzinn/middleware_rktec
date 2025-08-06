package com.example.rktec_middleware.ui.screens

import androidx.compose.runtime.*
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.rktec_middleware.data.model.Usuario
import com.example.rktec_middleware.viewmodel.*

private object AuthRoutes {
    const val LOGIN = "login"
    const val CADASTRO = "cadastro"
    const val ESQUECI_SENHA = "esqueci_senha"
    // A rota de Reativação não pertence mais a este fluxo
}

@Composable
fun FluxoAutenticacao(
    // A assinatura da função agora só precisa notificar sobre o sucesso do login.
    aoLoginSucesso: () -> Unit
) {
    val navController = rememberNavController()
    val loginViewModel: LoginViewModel = hiltViewModel()
    val cadastroViewModel: CadastroViewModel = hiltViewModel()
    val recuperarSenhaViewModel: RecuperarSenhaViewModel = hiltViewModel()

    val loginState by loginViewModel.loginState.collectAsState()

    // O LaunchedEffect que checava o estado "Inativo" foi removido.
    // A MainActivity cuidará disso.

    NavHost(navController = navController, startDestination = AuthRoutes.LOGIN) {
        composable(AuthRoutes.LOGIN) {
            // A chamada de onLoginSucesso foi simplificada
            TelaLogin(
                onLoginSucesso = { aoLoginSucesso() },
                onIrParaCadastro = { navController.navigate(AuthRoutes.CADASTRO) },
                onEsqueciSenha = { email ->
                    recuperarSenhaViewModel.setEmail(email)
                    navController.navigate(AuthRoutes.ESQUECI_SENHA)
                }
            )
        }
        composable(AuthRoutes.CADASTRO) {
            TelaCadastro(
                cadastroViewModel = cadastroViewModel,
                aoCadastroSucesso = { navController.popBackStack() }, // Volta para o login
                aoVoltarLogin = { navController.popBackStack() }
            )
        }
        composable(AuthRoutes.ESQUECI_SENHA) {
            TelaEsqueciSenha(
                aoVoltarLogin = { navController.popBackStack() }
            )
        }
    }
}