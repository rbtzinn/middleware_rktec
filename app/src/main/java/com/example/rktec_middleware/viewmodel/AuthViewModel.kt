package com.example.rktec_middleware.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rktec_middleware.data.model.Usuario
import com.example.rktec_middleware.repository.UsuarioRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

// Um estado para deixar claro o que está acontecendo com a autenticação.
sealed class AuthState {
    object Carregando : AuthState()
    data class Autenticado(val usuario: Usuario, val mapeamentoConcluido: Boolean) : AuthState()
    object NaoAutenticado : AuthState()
}

class AuthViewModel(
    private val usuarioRepository: UsuarioRepository
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Carregando)
    val authState: StateFlow<AuthState> = _authState

    // Função principal que verifica se o usuário já está logado no Firebase.
    fun verificarEstadoAutenticacao(isMapeamentoOk: suspend () -> Boolean) {
        viewModelScope.launch {
            _authState.value = AuthState.Carregando
            val firebaseUser = FirebaseAuth.getInstance().currentUser

            if (firebaseUser?.email != null) {
                // Usuário está logado no Firebase, vamos buscar os dados no Room.
                val usuarioLocal = usuarioRepository.buscarPorEmail(firebaseUser.email!!)
                if (usuarioLocal != null && usuarioLocal.ativo) {
                    val mapeamentoConcluido = isMapeamentoOk()
                    _authState.value = AuthState.Autenticado(usuarioLocal, mapeamentoConcluido)
                } else {
                    // Se não encontrar no Room ou estiver inativo, desloga.
                    FirebaseAuth.getInstance().signOut()
                    _authState.value = AuthState.NaoAutenticado
                }
            } else {
                // Nenhum usuário logado no Firebase.
                _authState.value = AuthState.NaoAutenticado
            }
        }
    }

    // Função chamada após o login manual ser bem-sucedido.
    fun onLoginSucesso(usuario: Usuario, mapeamentoConcluido: Boolean) {
        _authState.value = AuthState.Autenticado(usuario, mapeamentoConcluido)
    }

    // Função para atualizar o estado do mapeamento após a importação.
    fun setMapeamentoConcluido(concluido: Boolean) {
        if (_authState.value is AuthState.Autenticado) {
            val estadoAtual = _authState.value as AuthState.Autenticado
            _authState.value = estadoAtual.copy(mapeamentoConcluido = concluido)
        }
    }

    // Função de logout agora também limpa o estado do Firebase.
    fun logout() {
        viewModelScope.launch {
            FirebaseAuth.getInstance().signOut() // Importante: Fazer logout do Firebase.
            _authState.value = AuthState.NaoAutenticado
        }
    }

    // Função para recarregar os dados do usuário se forem editados.
    fun recarregarUsuario() {
        if (_authState.value is AuthState.Autenticado) {
            viewModelScope.launch {
                val estadoAtual = _authState.value as AuthState.Autenticado
                val usuarioAtualizado = usuarioRepository.buscarPorEmail(estadoAtual.usuario.email)
                if (usuarioAtualizado != null) {
                    _authState.value = estadoAtual.copy(usuario = usuarioAtualizado)
                } else {
                    logout() // Se o usuário for deletado, faz logout.
                }
            }
        }
    }
}