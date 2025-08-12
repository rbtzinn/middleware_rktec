package com.example.rktec_middleware.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rktec_middleware.data.model.TipoUsuario
import com.example.rktec_middleware.data.model.Usuario
import com.example.rktec_middleware.data.repository.AuthRepository
import com.example.rktec_middleware.repository.InventarioRepository
import com.example.rktec_middleware.repository.UsuarioRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await
import kotlin.onFailure
import kotlin.onSuccess

// ##### PASSO 1: ADICIONAR O NOVO ESTADO "INATIVO" #####
sealed class AuthState {
    object Carregando : AuthState()
    object NaoAutenticado : AuthState()
    data class Autenticado(val usuario: Usuario, val empresaJaConfigurada: Boolean) : AuthState()
    data class AguardandoVerificacao(val email: String?) : AuthState()
    data class Inativo(val usuario: Usuario) : AuthState() // Usuário existe mas está com ativo = false
}

// State para controlar a UI da tela de reativação
sealed class ReativacaoState {
    object Idle : ReativacaoState()
    object Loading : ReativacaoState()
    object Success : ReativacaoState()
    data class Error(val message: String) : ReativacaoState()
}


@HiltViewModel
class AuthViewModel @Inject constructor(
    private val usuarioRepository: UsuarioRepository,
    private val inventarioRepository: InventarioRepository
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Carregando)
    val authState: StateFlow<AuthState> = _authState

    private val _reativacaoState = MutableStateFlow<ReativacaoState>(ReativacaoState.Idle)
    val reativacaoState: StateFlow<ReativacaoState> = _reativacaoState.asStateFlow()

    fun verificarEstadoAutenticacao() {
        viewModelScope.launch {
            _authState.value = AuthState.Carregando
            val firebaseUser = FirebaseAuth.getInstance().currentUser

            if (firebaseUser == null) {
                _authState.value = AuthState.NaoAutenticado
                return@launch
            }

            // Garante que temos o status de verificação mais recente do usuário
            firebaseUser.reload().addOnCompleteListener { task ->
                if (!task.isSuccessful) {
                    logout()
                    return@addOnCompleteListener
                }

                // ##### VERIFICAÇÃO DE E-MAIL REATIVADA #####
                if (!firebaseUser.isEmailVerified) {
                    _authState.value = AuthState.AguardandoVerificacao(firebaseUser.email)
                    return@addOnCompleteListener
                }

                // Se o e-mail está verificado, continuamos para a lógica do app
                escutarDadosDoUsuario(firebaseUser.email!!)
            }
        }
    }

    // Função auxiliar para evitar repetição de código
    private fun escutarDadosDoUsuario(email: String) {
        viewModelScope.launch {
            usuarioRepository.escutarMudancasUsuario(email)
                .collect { usuarioDaNuvem ->
                    if (usuarioDaNuvem != null) {
                        if (usuarioDaNuvem.ativo) {
                            val empresa = usuarioRepository.buscarEmpresaPorId(usuarioDaNuvem.companyId)
                            val empresaConfigurada = empresa?.planilhaImportada ?: false

                            if (empresaConfigurada && !inventarioRepository.temInventarioLocal(usuarioDaNuvem.companyId)) {
                                empresa?.inventarioJsonPath?.let { path ->
                                    inventarioRepository.baixarInventarioEArmazenar(usuarioDaNuvem.companyId, path)
                                }
                            }
                            _authState.value = AuthState.Autenticado(usuarioDaNuvem, empresaConfigurada)
                        } else {
                            _authState.value = AuthState.Inativo(usuarioDaNuvem)
                        }
                    } else {
                        logout()
                    }
                }
        }
    }

    // ##### NOVA FUNÇÃO PARA O TIMER #####
    fun reenviarEmailDeVerificacao() {
        FirebaseAuth.getInstance().currentUser?.sendEmailVerification()
    }

    fun reativarComNovoCodigo(novoCodigo: String) {
        viewModelScope.launch {
            _reativacaoState.value = ReativacaoState.Loading
            val estadoAtual = _authState.value
            if (estadoAtual is AuthState.Inativo) {
                val resultado = usuarioRepository.reativarETransferirUsuario(estadoAtual.usuario, novoCodigo)
                resultado.onSuccess {
                    _reativacaoState.value = ReativacaoState.Success
                    verificarEstadoAutenticacao()
                }
                resultado.onFailure { erro ->
                    _reativacaoState.value = ReativacaoState.Error(erro.message ?: "Erro desconhecido")
                }
            } else {
                _reativacaoState.value = ReativacaoState.Error("Estado inválido para reativação.")
            }
        }
    }

    // --- Suas funções existentes (sem alterações) ---
    fun onLoginSucesso() {
        verificarEstadoAutenticacao()
    }

    fun setEmpresaConfigurada(configurada: Boolean) {
        val estadoAtual = _authState.value
        if (estadoAtual is AuthState.Autenticado) {
            _authState.value = estadoAtual.copy(empresaJaConfigurada = configurada)
        }
    }

    fun logout() {
        viewModelScope.launch {
            FirebaseAuth.getInstance().signOut()
            _authState.value = AuthState.NaoAutenticado
        }
    }

    fun promoverUsuarioAtualParaAdmin() {
        viewModelScope.launch {
            val estadoAtual = _authState.value
            if (estadoAtual is AuthState.Autenticado) {
                val usuario = estadoAtual.usuario
                if (usuario.tipo != TipoUsuario.ADMIN) {
                    val usuarioAdmin = usuario.copy(tipo = TipoUsuario.ADMIN)
                    usuarioRepository.atualizarUsuario(usuarioAdmin)
                }
            }
        }
    }
}