package com.example.rktec_middleware.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rktec_middleware.data.model.TipoUsuario
import com.example.rktec_middleware.data.model.Usuario
import com.example.rktec_middleware.repository.InventarioRepository
import com.example.rktec_middleware.repository.UsuarioRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

sealed class AuthState {
    object Carregando : AuthState()
    object NaoAutenticado : AuthState()
    data class Autenticado(val usuario: Usuario, val empresaJaConfigurada: Boolean) : AuthState()
}

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val usuarioRepository: UsuarioRepository,
    private val inventarioRepository: InventarioRepository
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Carregando)
    val authState: StateFlow<AuthState> = _authState

    fun verificarEstadoAutenticacao() {
        viewModelScope.launch {
            _authState.value = AuthState.Carregando
            val firebaseUser = FirebaseAuth.getInstance().currentUser

            if (firebaseUser?.email != null) {
                usuarioRepository.escutarMudancasUsuario(firebaseUser.email!!)
                    .collect { usuarioDaNuvem ->
                        if (usuarioDaNuvem != null) {
                            usuarioRepository.cadastrarUsuario(usuarioDaNuvem)

                            if (usuarioDaNuvem.ativo) {
                                val empresa = usuarioRepository.buscarEmpresaPorId(usuarioDaNuvem.companyId)
                                val empresaConfigurada = empresa?.planilhaImportada ?: false

                                // ##### LÓGICA DE SINCRONIZAÇÃO INTELIGENTE #####
                                // Só baixa o inventário se a empresa estiver configurada E
                                // se não houver dados salvos localmente.
                                if (empresaConfigurada && !inventarioRepository.temInventarioLocal(usuarioDaNuvem.companyId)) {
                                    empresa?.inventarioJsonPath?.let { path ->
                                        inventarioRepository.baixarInventarioEArmazenar(usuarioDaNuvem.companyId, path)
                                    }
                                }

                                _authState.value = AuthState.Autenticado(usuarioDaNuvem, empresaConfigurada)
                            } else {
                                logout()
                            }
                        } else {
                            logout()
                        }
                    }
            } else {
                _authState.value = AuthState.NaoAutenticado
            }
        }
    }

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