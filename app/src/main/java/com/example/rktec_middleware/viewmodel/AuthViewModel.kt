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

    fun recarregarUsuario(email: String) {
        viewModelScope.launch {
            val usuario = usuarioRepository.buscarPorEmail(email)
            if (usuario != null) {
                _usuarioAutenticado.value = usuario
            }
        }
    }

    fun setMapeamentoConcluido(concluido: Boolean) {
        _mapeamentoConcluido.value = concluido
    }

    fun login(context: Context, usuario: Usuario) {
        viewModelScope.launch {
            _usuarioAutenticado.value = usuario

            // Salvando o login no SharedPreferences
            val prefs = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
            prefs.edit().putString("usuario_email", usuario.email).apply()

            // (Opcional) também marca mapeamento como feito
            setMapeamentoConcluido(true)
        }
    }

    fun autoLogin(context: Context, isMapeamentoOk: suspend () -> Boolean) {
        viewModelScope.launch {
            val prefs = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
            val email = prefs.getString("usuario_email", null)

            if (email != null) {
                val usuario = usuarioRepository.buscarPorEmail(email)
                if (usuario != null) {
                    _usuarioAutenticado.value = usuario
                    setMapeamentoConcluido(isMapeamentoOk())
                }
            }
        }
    }

    fun logout(context: Context) {
        viewModelScope.launch {
            _usuarioAutenticado.value = null
            context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
                .edit()
                .clear()
                .apply()
            setMapeamentoConcluido(false)
        }
    }



}
