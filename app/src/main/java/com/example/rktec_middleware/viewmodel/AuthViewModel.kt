package com.example.rktec_middleware.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rktec_middleware.data.model.TipoUsuario
import com.example.rktec_middleware.data.model.Usuario
import com.example.rktec_middleware.repository.InventarioRepository
import com.example.rktec_middleware.repository.UsuarioRepository
import com.example.rktec_middleware.viewmodel.AuthState.Autenticado
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

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
                            // Usuário existe no Firestore
                            usuarioRepository.cadastrarUsuario(usuarioDaNuvem)

                            if (usuarioDaNuvem.ativo) {
                                // Usuário está ativo, verificar a empresa
                                val empresa = usuarioRepository.buscarEmpresaPorId(usuarioDaNuvem.companyId)
                                val empresaConfigurada = empresa?.planilhaImportada ?: false

                                if (empresaConfigurada) {
                                    inventarioRepository.syncInventarioDoFirestore(usuarioDaNuvem.companyId)
                                }
                                _authState.value = AuthState.Autenticado(usuarioDaNuvem, empresaConfigurada)

                            } else {
                                // Usuário foi desativado
                                FirebaseAuth.getInstance().signOut()
                                _authState.value = AuthState.NaoAutenticado
                            }
                        } else {
                            // Usuário autenticado no Firebase Auth, mas sem registro no Firestore.
                            // Isso pode acontecer se o registro for excluído manualmente.
                            FirebaseAuth.getInstance().signOut()
                            _authState.value = AuthState.NaoAutenticado
                        }
                    }
            } else {
                // Nenhum usuário logado no Firebase Auth
                _authState.value = AuthState.NaoAutenticado
            }
        }
    }

    fun onLoginSucesso(usuario: Usuario, inventarioSincronizado: Boolean) {
        _authState.value = Autenticado(usuario, inventarioSincronizado)
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
            if (estadoAtual is Autenticado) {
                val usuario = estadoAtual.usuario
                if (usuario.tipo != TipoUsuario.ADMIN) {
                    val usuarioAdmin = usuario.copy(tipo = TipoUsuario.ADMIN)
                    usuarioRepository.atualizarUsuario(usuarioAdmin)
                    }
                }
            }
        }
    }
