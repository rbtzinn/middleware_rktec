package com.example.rktec_middleware.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rktec_middleware.data.model.Usuario
import com.example.rktec_middleware.repository.UsuarioRepository
import com.example.rktec_middleware.util.SenhaUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class LoginState {
    object Idle : LoginState()
    object Loading : LoginState()
    data class Sucesso(val usuario: Usuario) : LoginState()
    data class Erro(val mensagem: String) : LoginState()
}

class LoginViewModel(private val usuarioRepo: UsuarioRepository) : ViewModel() {
    private val _loginState = MutableStateFlow<LoginState>(LoginState.Idle)
    val loginState: StateFlow<LoginState> = _loginState

    fun autenticar(nome: String, senha: String) {
        viewModelScope.launch {
            _loginState.value = LoginState.Loading
            val usuario = usuarioRepo.buscarPorNome(nome)
            if (usuario == null) {
                _loginState.value = LoginState.Erro("Usuário não encontrado")
            } else if (usuario.senhaHash != SenhaUtils.hashSenha(senha)) {
                _loginState.value = LoginState.Erro("Senha incorreta")
            } else {
                _loginState.value = LoginState.Sucesso(usuario)
            }
        }
    }
}
