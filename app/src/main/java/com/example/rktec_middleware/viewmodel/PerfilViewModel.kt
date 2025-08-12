package com.example.rktec_middleware.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rktec_middleware.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PerfilUiState(
    val isLoadingPasswordReset: Boolean = false,
    val showLogoutDialog: Boolean = false,
    val toastMessage: String? = null
)

@HiltViewModel
class PerfilViewModel @Inject constructor(
    // Injeta o repositório, que é a fonte única da verdade para autenticação
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PerfilUiState())
    val uiState = _uiState.asStateFlow()

    fun onSendPasswordResetEmail() {
        val userEmail = authRepository.getCurrentUser()?.email
        if (userEmail == null) {
            _uiState.update { it.copy(toastMessage = "Não foi possível identificar o usuário.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingPasswordReset = true) }
            val success = authRepository.sendPasswordResetEmail(userEmail)
            val message = if (success) "E-mail de redefinição enviado!" else "Falha ao enviar e-mail."
            _uiState.update {
                it.copy(isLoadingPasswordReset = false, toastMessage = message)
            }
        }
    }

    fun onLogoutClicked() {
        _uiState.update { it.copy(showLogoutDialog = true) }
    }

    fun onConfirmLogout() {
        authRepository.logout()
        _uiState.update { it.copy(showLogoutDialog = false) }
    }

    fun onDismissLogoutDialog() {
        _uiState.update { it.copy(showLogoutDialog = false) }
    }

    fun onToastShown() {
        _uiState.update { it.copy(toastMessage = null) }
    }
}
