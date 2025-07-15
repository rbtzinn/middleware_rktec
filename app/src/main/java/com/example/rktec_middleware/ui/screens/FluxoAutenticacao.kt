// ui/screens/FluxoAutenticacao.kt
package com.example.rktec_middleware.ui.screens

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.rktec_middleware.data.model.Usuario
import com.example.rktec_middleware.repository.UsuarioRepository
import com.example.rktec_middleware.viewmodel.*

private object AuthRoutes {
    const val LOGIN = "login"
    const val CADASTRO = "cadastro"
    const val ESQUECI_SENHA = "esqueci_senha"
}

@Composable
fun FluxoAutenticacao(
    usuarioRepository: UsuarioRepository,
    aoLoginSucesso: (Usuario) -> Unit
) {
    val navController = rememberNavController()
    val loginViewModel: LoginViewModel = viewModel(factory = LoginViewModelFactory(usuarioRepository))
    val cadastroViewModel: CadastroViewModel = viewModel(factory = CadastroViewModelFactory(usuarioRepository))
    val recuperarSenhaViewModel: RecuperarSenhaViewModel = viewModel()

    NavHost(navController = navController, startDestination = AuthRoutes.LOGIN) {
        composable(AuthRoutes.LOGIN) {
            loginViewModel.resetarEstado()
            TelaLogin(
                viewModel = loginViewModel,
                onLoginSucesso = aoLoginSucesso,
                onIrParaCadastro = { navController.navigate(AuthRoutes.CADASTRO) },
                onEsqueciSenha = { email ->
                    recuperarSenhaViewModel.setEmail(email) // Agora esta função existe
                    navController.navigate(AuthRoutes.ESQUECI_SENHA)
                }
            )
        }
        composable(AuthRoutes.CADASTRO) {
            TelaCadastro(
                cadastroViewModel = cadastroViewModel,
                aoCadastroSucesso = { navController.navigate(AuthRoutes.LOGIN) { popUpTo(AuthRoutes.LOGIN) { inclusive = true } } },
                aoVoltarLogin = { navController.popBackStack() }
            )
        }
        composable(AuthRoutes.ESQUECI_SENHA) {
            TelaEsqueciSenha(
                aoEnviarCodigo = { email ->
                    recuperarSenhaViewModel.enviarResetEmail(email)
                    navController.popBackStack()
                },
                aoVoltarLogin = { navController.popBackStack() }
            )
        }
    }
}