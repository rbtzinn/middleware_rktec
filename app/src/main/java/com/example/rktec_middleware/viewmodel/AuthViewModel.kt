package com.example.rktec_middleware.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rktec_middleware.data.model.Usuario
import com.example.rktec_middleware.repository.UsuarioRepository
import com.example.rktec_middleware.util.UsuarioLogadoManager
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AuthViewModel(
    private val usuarioRepository: UsuarioRepository
) : ViewModel() {

    private var autoLoginJaRodou = false

    private val _usuarioAutenticado = MutableStateFlow<Usuario?>(null)
    val usuarioAutenticado: StateFlow<Usuario?> = _usuarioAutenticado

    private val _mapeamentoConcluido = MutableStateFlow(false)
    val mapeamentoConcluido: StateFlow<Boolean> = _mapeamentoConcluido

    fun login(context: Context, usuario: Usuario) {
        Log.d("RKTEC_DEBUG", "CHAMOU AuthViewModel.login com ${usuario.email}")
        _usuarioAutenticado.value = usuario
        UsuarioLogadoManager.salvarUsuario(context, usuario.email)
    }

    fun recarregarUsuario(email: String) {
        viewModelScope.launch {
            val usuario = usuarioRepository.buscarPorEmail(email)
            if (usuario != null) {
                _usuarioAutenticado.value = usuario
            }
        }
    }

    fun logout(context: Context) {
        UsuarioLogadoManager.limparUsuario(context)
        _usuarioAutenticado.value = null
        _mapeamentoConcluido.value = false
        FirebaseAuth.getInstance().signOut()
    }

    fun setMapeamentoConcluido(concluido: Boolean) {
        _mapeamentoConcluido.value = concluido
    }

    fun autoLogin(context: Context, onMapeamento: suspend () -> Boolean) {
        if (autoLoginJaRodou) return
        autoLoginJaRodou = true
        viewModelScope.launch {
            val emailSalvo = UsuarioLogadoManager.obterUsuario(context)
            if (!emailSalvo.isNullOrBlank()) {
                val usuario = usuarioRepository.buscarPorEmail(emailSalvo)
                if (usuario != null) {
                    _usuarioAutenticado.value = usuario
                    val mapeamento = onMapeamento()
                    _mapeamentoConcluido.value = mapeamento
                }
            }
        }
    }

}
