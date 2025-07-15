package com.example.rktec_middleware.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

sealed class RecuperarSenhaState {
    object Idle : RecuperarSenhaState()
    object Enviando : RecuperarSenhaState()
    object Sucesso : RecuperarSenhaState()
    data class Erro(val mensagem: String) : RecuperarSenhaState()
}

class RecuperarSenhaViewModel : ViewModel() {
    private val _state = MutableStateFlow<RecuperarSenhaState>(RecuperarSenhaState.Idle)
    val state: StateFlow<RecuperarSenhaState> = _state

    private val _email = mutableStateOf("")
    val email: State<String> = _email

    fun setEmail(newEmail: String) {
        _email.value = newEmail
    }

    fun enviarResetEmail(email: String) {
        _state.value = RecuperarSenhaState.Enviando
        FirebaseAuth.getInstance().sendPasswordResetEmail(email)
            .addOnSuccessListener {
                _state.value = RecuperarSenhaState.Sucesso
            }
            .addOnFailureListener { e ->
                _state.value = RecuperarSenhaState.Erro(e.message ?: "Erro ao enviar email!")
            }
    }
}
