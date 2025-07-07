package com.example.rktec_middleware.ui.screens

import androidx.compose.runtime.*
import com.example.rktec_middleware.data.model.Usuario
import com.example.rktec_middleware.viewmodel.CadastroViewModel
import com.example.rktec_middleware.viewmodel.LoginViewModel
import com.example.rktec_middleware.viewmodel.RecuperarSenhaViewModel

enum class TelaAutenticacao {
    LOGIN, ESQUECI_SENHA, CODIGO, REDEFINIR, CADASTRO
}


@Composable
fun FluxoAutenticacao(
    loginViewModel: LoginViewModel,
    recuperarSenhaViewModel: RecuperarSenhaViewModel,
    aoLoginSucesso: (Usuario) -> Unit,
    cadastroViewModel: CadastroViewModel
) {
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
                recuperarSenhaViewModel.enviarCodigo(email)
                telaAtual = TelaAutenticacao.CODIGO
            }
        )

        TelaAutenticacao.CODIGO -> TelaCodigoVerificacao(
            aoValidarCodigo = { codigo ->
                if (recuperarSenhaViewModel.validarCodigo(codigo)) {
                    telaAtual = TelaAutenticacao.REDEFINIR
                }
            }
        )

        TelaAutenticacao.REDEFINIR -> TelaRedefinirSenha(
            aoSalvar = { novaSenha ->
                recuperarSenhaViewModel.redefinirSenha(novaSenha)
                telaAtual = TelaAutenticacao.LOGIN
            }
        )
    }
}
