package com.example.rktec_middleware.viewmodel

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow



sealed class CadastroState {
    object Idle : CadastroState()
    object Loading : CadastroState()
    object Sucesso : CadastroState()
    data class Erro(val mensagem: String) : CadastroState()
}

class CadastroViewModel : ViewModel() {
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
                    _cadastroState.value = CadastroState.Sucesso
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

