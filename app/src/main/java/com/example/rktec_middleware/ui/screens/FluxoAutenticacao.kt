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
    const val REATIVACAO = "reativacao"
}

@Composable
fun FluxoAutenticacao(
    aoLoginSucesso: (Usuario) -> Unit
) {
    val navController = rememberNavController()
    val loginViewModel: LoginViewModel = hiltViewModel()
    val cadastroViewModel: CadastroViewModel = hiltViewModel()
    val recuperarSenhaViewModel: RecuperarSenhaViewModel = hiltViewModel()

    val loginState by loginViewModel.loginState.collectAsState()

    // MUDANÇA: Navega para a tela de reativação quando o estado muda
    LaunchedEffect(loginState) {
        if (loginState is LoginState.Inativo) {
            navController.navigate(AuthRoutes.REATIVACAO)
        }
    }

    NavHost(navController = navController, startDestination = AuthRoutes.LOGIN) {
        composable(AuthRoutes.LOGIN) {
            TelaLogin(
                viewModel = loginViewModel,
                onLoginSucesso = aoLoginSucesso,
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

        composable(AuthRoutes.REATIVACAO) {
            TelaReativacao(
                viewModel = loginViewModel,
                onCancel = {
                    loginViewModel.resetarEstado()
                    navController.popBackStack()
                }
            )
        }
    }
}