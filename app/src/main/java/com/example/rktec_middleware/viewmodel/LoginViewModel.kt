package com.example.rktec_middleware.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rktec_middleware.data.model.TipoUsuario
import com.example.rktec_middleware.data.model.Usuario
import com.example.rktec_middleware.repository.UsuarioRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class LoginState {
    object Idle : LoginState()
    object Loading : LoginState()
    data class Sucesso(val usuario: Usuario) : LoginState()
    data class Erro(val mensagem: String) : LoginState()
    data class Inativo(val usuario: Usuario) : LoginState()
}

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val usuarioRepository: UsuarioRepository
) : ViewModel() {
    private val _loginState = MutableStateFlow<LoginState>(LoginState.Idle)
    val loginState: StateFlow<LoginState> = _loginState.asStateFlow()

    private val _sugestoesEmail = MutableStateFlow<List<String>>(emptyList())
    val sugestoesEmail: StateFlow<List<String>> = _sugestoesEmail.asStateFlow()

    fun carregarSugestoesDeEmail() {
        viewModelScope.launch {
            _sugestoesEmail.value = usuarioRepository.listarEmails()
        }
    }

    fun autenticar(email: String, senha: String) {
        _loginState.value = LoginState.Loading
        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, senha)
            .addOnSuccessListener { result ->
                viewModelScope.launch {
                    // ... (lógica de sincronização que já fizemos)
                    val firebaseUser = result.user ?: return@launch
                    val userEmail = firebaseUser.email ?: ""
                    var usuarioLocal = usuarioRepository.buscarPorEmail(userEmail)
                    if (usuarioLocal == null) {
                        val usuarioFirestore = usuarioRepository.buscarUsuarioNoFirestore(userEmail)
                        if (usuarioFirestore != null) {
                            usuarioRepository.cadastrarUsuario(usuarioFirestore)
                            usuarioLocal = usuarioFirestore
                        }
                    }

                    if (usuarioLocal == null) {
                        FirebaseAuth.getInstance().signOut()
                        _loginState.value = LoginState.Erro("Usuário autenticado, mas não encontrado no sistema. Fale com o administrador.")
                    } else if (!usuarioLocal.ativo) {
                        // MUDANÇA: Em vez de erro, emitimos o estado 'Inativo'
                        _loginState.value = LoginState.Inativo(usuarioLocal)
                    } else {
                        _loginState.value = LoginState.Sucesso(usuarioLocal)
                    }
                }
            }
            .addOnFailureListener { e ->
                _loginState.value = LoginState.Erro(e.message ?: "E-mail ou senha inválidos!")
            }
    }

    // NOVA FUNÇÃO: Para reativar e transferir o usuário
    fun reativarETransferir(novoCodigoEmpresa: String) {
        val estadoAtual = _loginState.value
        if (estadoAtual !is LoginState.Inativo) return

        _loginState.value = LoginState.Loading // Mostra o loading

        viewModelScope.launch {
            val resultado = usuarioRepository.reativarETransferirUsuario(estadoAtual.usuario, novoCodigoEmpresa)
            resultado.onSuccess { usuarioAtualizado ->
                _loginState.value = LoginState.Sucesso(usuarioAtualizado) // Sucesso!
            }
            resultado.onFailure { erro ->
                _loginState.value = LoginState.Erro(erro.message ?: "Falha ao reativar conta.")
            }
        }
    }

    fun resetarEstado() {
        _loginState.value = LoginState.Idle
    }
}