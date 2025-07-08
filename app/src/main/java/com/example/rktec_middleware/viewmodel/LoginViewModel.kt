package com.example.rktec_middleware.viewmodel

import androidx.lifecycle.ViewModel
import com.example.rktec_middleware.data.model.TipoUsuario
import com.example.rktec_middleware.data.model.Usuario
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow



sealed class LoginState {
    object Idle : LoginState()
    object Loading : LoginState()
    data class Sucesso(val usuario: Usuario) : LoginState()
    data class Erro(val mensagem: String) : LoginState()
}

class LoginViewModel : ViewModel() {
    private val _loginState = MutableStateFlow<LoginState>(LoginState.Idle)
    val loginState: StateFlow<LoginState> = _loginState

    fun autenticar(email: String, senha: String) {
        _loginState.value = LoginState.Loading
        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, senha)
            .addOnSuccessListener { result ->
                val user = result.user
                if (user != null) {
                    // Aqui você pode criar o seu objeto Usuario se quiser, com displayName e email do Firebase
                    val usuario = Usuario(
                        nome = user.displayName ?: "",
                        email = user.email ?: "",
                        senhaHash = "",
                        tipo = TipoUsuario.MEMBRO
                    )
                    _loginState.value = LoginState.Sucesso(usuario)
                } else {
                    _loginState.value = LoginState.Erro("Usuário não encontrado!")
                }
            }
            .addOnFailureListener { e ->
                _loginState.value = LoginState.Erro(e.message ?: "Erro ao logar!")
            }
    }
}
