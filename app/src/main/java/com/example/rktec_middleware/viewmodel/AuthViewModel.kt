package com.example.rktec_middleware.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rktec_middleware.data.model.Usuario
import com.example.rktec_middleware.repository.UsuarioRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

// Sealed class AuthState permanece a mesma...
sealed class AuthState {
    object Carregando : AuthState()
    data class Autenticado(val usuario: Usuario, val mapeamentoConcluido: Boolean) : AuthState()
    object NaoAutenticado : AuthState()
}


@HiltViewModel // Anotação principal
class AuthViewModel @Inject constructor( // Injeção no construtor
    private val usuarioRepository: UsuarioRepository
) : ViewModel() {

    // O corpo da classe permanece exatamente o mesmo
    private val _authState = MutableStateFlow<AuthState>(AuthState.Carregando)
    val authState: StateFlow<AuthState> = _authState

    fun verificarEstadoAutenticacao(isMapeamentoOk: suspend () -> Boolean) {
        viewModelScope.launch {
            _authState.value = AuthState.Carregando
            val firebaseUser = FirebaseAuth.getInstance().currentUser

            if (firebaseUser?.email != null) {
                val usuarioLocal = usuarioRepository.buscarPorEmail(firebaseUser.email!!)
                if (usuarioLocal != null && usuarioLocal.ativo) {
                    val mapeamentoConcluido = isMapeamentoOk()
                    _authState.value = AuthState.Autenticado(usuarioLocal, mapeamentoConcluido)
                } else {
                    FirebaseAuth.getInstance().signOut()
                    _authState.value = AuthState.NaoAutenticado
                }
            } else {
                _authState.value = AuthState.NaoAutenticado
            }
        }
    }

    fun onLoginSucesso(usuario: Usuario, mapeamentoConcluido: Boolean) {
        _authState.value = AuthState.Autenticado(usuario, mapeamentoConcluido)
    }

    fun setMapeamentoConcluido(concluido: Boolean) {
        if (_authState.value is AuthState.Autenticado) {
            val estadoAtual = _authState.value as AuthState.Autenticado
            _authState.value = estadoAtual.copy(mapeamentoConcluido = concluido)
        }
    }

    fun logout() {
        viewModelScope.launch {
            FirebaseAuth.getInstance().signOut()
            _authState.value = AuthState.NaoAutenticado
        }
    }

    fun recarregarUsuario() {
        if (_authState.value is AuthState.Autenticado) {
            viewModelScope.launch {
                val estadoAtual = _authState.value as AuthState.Autenticado
                val usuarioAtualizado = usuarioRepository.buscarPorEmail(estadoAtual.usuario.email)
                if (usuarioAtualizado != null) {
                    _authState.value = estadoAtual.copy(usuario = usuarioAtualizado)
                } else {
                    logout()
                }
            }
        }
    }
}