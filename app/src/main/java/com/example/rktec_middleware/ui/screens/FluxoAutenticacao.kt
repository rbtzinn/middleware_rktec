package com.example.rktec_middleware.ui.screens

import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.rktec_middleware.data.model.Usuario
import com.example.rktec_middleware.repository.UsuarioRepository
import com.example.rktec_middleware.viewmodel.CadastroViewModel
import com.example.rktec_middleware.viewmodel.CadastroViewModelFactory
import com.example.rktec_middleware.viewmodel.LoginViewModel
import com.example.rktec_middleware.viewmodel.LoginViewModelFactory
import com.example.rktec_middleware.viewmodel.RecuperarSenhaViewModel

enum class TelaAutenticacao {
    LOGIN, ESQUECI_SENHA, CADASTRO
}

@Composable
fun FluxoAutenticacao(
    usuarioRepository: UsuarioRepository,
    aoLoginSucesso: (Usuario) -> Unit
) {
    // Os ViewModels agora recebem o repository corretamente!
    val loginViewModel: LoginViewModel = viewModel(factory = LoginViewModelFactory(usuarioRepository))
    val cadastroViewModel: CadastroViewModel = viewModel(factory = CadastroViewModelFactory(usuarioRepository))
    val recuperarSenhaViewModel: RecuperarSenhaViewModel = viewModel() // se não usa repo, pode manter assim

    var telaAtual by remember { mutableStateOf(TelaAutenticacao.LOGIN) }
    var emailSalvo by remember { mutableStateOf("") }

    when (telaAtual) {
        TelaAutenticacao.LOGIN -> TelaLogin(
            onLoginSucesso = {
                aoLoginSucesso(it)
            },
            onEsqueciSenha = { email ->
                emailSalvo = email
                telaAtual = TelaAutenticacao.ESQUECI_SENHA
            },
            onIrParaCadastro = { telaAtual = TelaAutenticacao.CADASTRO },
            viewModel = loginViewModel
        )
        TelaAutenticacao.CADASTRO -> TelaCadastro(
            cadastroViewModel = cadastroViewModel,
            aoCadastroSucesso = { telaAtual = TelaAutenticacao.LOGIN },
            aoVoltarLogin = { telaAtual = TelaAutenticacao.LOGIN }
        )

        TelaAutenticacao.ESQUECI_SENHA -> TelaEsqueciSenha(
            aoEnviarCodigo = { email ->
                emailSalvo = email
                recuperarSenhaViewModel.enviarResetEmail(email)
                telaAtual = TelaAutenticacao.LOGIN
            },
            aoVoltarLogin = { telaAtual = TelaAutenticacao.LOGIN }
        )
    }
}
