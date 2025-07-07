package com.example.rktec_middleware.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rktec_middleware.data.model.TipoUsuario
import com.example.rktec_middleware.data.model.Usuario
import com.example.rktec_middleware.repository.UsuarioRepository
import com.example.rktec_middleware.util.SenhaUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class CadastroState {
    object Idle : CadastroState()
    object Loading : CadastroState()
    object Sucesso : CadastroState()
    data class Erro(val mensagem: String) : CadastroState()
}

class CadastroViewModel(
    private val usuarioRepo: UsuarioRepository
) : ViewModel() {
    private val _cadastroState = MutableStateFlow<CadastroState>(CadastroState.Idle)
    val cadastroState: StateFlow<CadastroState> = _cadastroState

    fun cadastrar(nome: String, email: String, senha: String, tipo: TipoUsuario = TipoUsuario.MEMBRO) {
        viewModelScope.launch {
            _cadastroState.value = CadastroState.Loading

            val jaExiste = usuarioRepo.buscarPorEmail(email)
            if (jaExiste != null) {
                _cadastroState.value = CadastroState.Erro("Já existe um usuário com esse e-mail.")
                return@launch
            }

            val usuario = Usuario(
                nome = nome,
                email = email,
                senhaHash = SenhaUtils.hashSenha(senha),
                tipo = tipo
            )
            usuarioRepo.inserir(usuario)
            _cadastroState.value = CadastroState.Sucesso
        }
    }

    fun resetar() {
        _cadastroState.value = CadastroState.Idle
    }
}
