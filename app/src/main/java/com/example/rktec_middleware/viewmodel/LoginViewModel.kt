package com.example.rktec_middleware.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rktec_middleware.data.model.Usuario
import com.example.rktec_middleware.repository.UsuarioRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class LoginState {
    object Idle : LoginState()
    object Loading : LoginState()
    data class Sucesso(val usuario: Usuario) : LoginState()
    data class Erro(val mensagem: String) : LoginState()
}

class LoginViewModel(
    private val usuarioRepository: UsuarioRepository // agora injeta o repo!
) : ViewModel() {
    private val _loginState = MutableStateFlow<LoginState>(LoginState.Idle)
    val loginState: StateFlow<LoginState> = _loginState

    fun autenticar(email: String, senha: String) {
        _loginState.value = LoginState.Loading
        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, senha)
            .addOnSuccessListener { result ->
                val user = result.user
                if (user != null) {
                    // Agora faz a busca no Room!
                    viewModelScope.launch {
                        val usuarioRoom = usuarioRepository.buscarPorEmail(user.email ?: "")
                        if (usuarioRoom == null) {
                            FirebaseAuth.getInstance().signOut()
                            _loginState.value = LoginState.Erro("Usuário não encontrado no sistema, fale com o administrador.")
                        } else if (!usuarioRoom.ativo) {
                            FirebaseAuth.getInstance().signOut()
                            _loginState.value = LoginState.Erro("Usuário desativado. Peça ao administrador para reativar sua conta.")
                        } else {
                            _loginState.value = LoginState.Sucesso(usuarioRoom)
                        }
                    }
                } else {
                    _loginState.value = LoginState.Erro("Usuário não encontrado!")
                }
            }
            .addOnFailureListener { e ->
                _loginState.value = LoginState.Erro(e.message ?: "Erro ao logar!")
            }
    }
}
