package com.example.rktec_middleware.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rktec_middleware.data.model.TipoUsuario
import com.example.rktec_middleware.data.model.Usuario
import com.example.rktec_middleware.repository.UsuarioRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
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
    private val usuarioRepository: UsuarioRepository
) : ViewModel() {
    private val _cadastroState = MutableStateFlow<CadastroState>(CadastroState.Idle)
    val cadastroState: StateFlow<CadastroState> = _cadastroState

    fun cadastrar(nome: String, email: String, senha: String) {
        _cadastroState.value = CadastroState.Loading
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, senha)
            .addOnSuccessListener { result ->
                val user = result.user
                user?.updateProfile(
                    UserProfileChangeRequest.Builder().setDisplayName(nome).build()
                )?.addOnCompleteListener {
                    // Salva o usuário no Room depois de criar no Firebase Auth
                    viewModelScope.launch {
                        try {
                            val usuario = Usuario(
                                nome = nome,
                                email = email,
                                senhaHash = senha.hashCode().toString(), // Use um hash seguro depois!
                                tipo = TipoUsuario.MEMBRO,
                                ativo = true
                            )
                            usuarioRepository.cadastrarUsuario(usuario)
                            _cadastroState.value = CadastroState.Sucesso
                        } catch (e: Exception) {
                            _cadastroState.value = CadastroState.Erro("Erro ao salvar local: ${e.message}")
                        }
                    }
                }
            }
            .addOnFailureListener { e ->
                _cadastroState.value = CadastroState.Erro(e.message ?: "Erro ao cadastrar!")
            }
    }

    fun resetar() {
        _cadastroState.value = CadastroState.Idle
    }
}
