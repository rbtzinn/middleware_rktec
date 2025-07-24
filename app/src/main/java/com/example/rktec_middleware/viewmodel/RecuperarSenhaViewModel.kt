package com.example.rktec_middleware.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class RecuperarSenhaState {
    object Idle : RecuperarSenhaState()
    object Loading : RecuperarSenhaState()
    object Success : RecuperarSenhaState()
    data class Error(val message: String) : RecuperarSenhaState()
}

@HiltViewModel
class RecuperarSenhaViewModel @Inject constructor() : ViewModel() {

    private val _state = MutableStateFlow<RecuperarSenhaState>(RecuperarSenhaState.Idle)
    val state = _state.asStateFlow()

    var emailInicial: String? = null
        private set

    fun setEmail(email: String?) {
        if (!email.isNullOrBlank()) {
            emailInicial = email
        }
    }

    fun enviarResetEmail(email: String) {
        if (email.isBlank() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _state.value = RecuperarSenhaState.Error("Por favor, insira um e-mail válido.")
            return
        }

        _state.value = RecuperarSenhaState.Loading
        FirebaseAuth.getInstance().sendPasswordResetEmail(email)
            .addOnSuccessListener {
                _state.value = RecuperarSenhaState.Success
            }
            .addOnFailureListener { e ->
                _state.value = RecuperarSenhaState.Error(e.message ?: "Ocorreu um erro.")
            }
    }

    fun resetState() {
        _state.value = RecuperarSenhaState.Idle
    }
}