package com.example.rktec_middleware.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rktec_middleware.repository.UsuarioRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class RecuperarSenhaState {
    object Idle : RecuperarSenhaState()
    object Enviando : RecuperarSenhaState()
    data class CodigoEnviado(val codigo: String) : RecuperarSenhaState()
    data class Erro(val mensagem: String) : RecuperarSenhaState()
    object SenhaRedefinida : RecuperarSenhaState()
}

class RecuperarSenhaViewModel(private val usuarioRepo: UsuarioRepository) : ViewModel() {
    private val _state = MutableStateFlow<RecuperarSenhaState>(RecuperarSenhaState.Idle)
    val state: StateFlow<RecuperarSenhaState> = _state

    private var codigoGerado: String? = null
    private var emailSalvo: String? = null

    fun enviarCodigo(email: String) {
        viewModelScope.launch {
            _state.value = RecuperarSenhaState.Enviando
            val usuario = usuarioRepo.buscarPorEmail(email)
            if (usuario == null) {
                _state.value = RecuperarSenhaState.Erro("Usuário não encontrado")
                return@launch
            }
            codigoGerado = (100000..999999).random().toString()
            emailSalvo = email
            // Aqui tu enviaria o código pro email de verdade
            _state.value = RecuperarSenhaState.CodigoEnviado(codigoGerado!!)
        }
    }

    fun validarCodigo(codigo: String) = codigo == codigoGerado

    fun redefinirSenha(novaSenha: String) {
        viewModelScope.launch {
            val usuario = usuarioRepo.buscarPorEmail(emailSalvo ?: "")
            if (usuario != null) {
                val novoUsuario = usuario.copy(senhaHash = com.example.rktec_middleware.util.SenhaUtils.hashSenha(novaSenha))
                usuarioRepo.atualizar(novoUsuario)
                _state.value = RecuperarSenhaState.SenhaRedefinida
            } else {
                _state.value = RecuperarSenhaState.Erro("Usuário não encontrado")
            }
        }
    }
}
