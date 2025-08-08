package com.example.rktec_middleware.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rktec_middleware.data.db.AppDatabase
import com.example.rktec_middleware.data.model.LogGerenciamentoUsuario
import com.example.rktec_middleware.repository.UsuarioRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LogViewModel @Inject constructor(
    private val db: AppDatabase,
    private val usuarioRepository: UsuarioRepository, // Injeta o repo de usuário
    private val firebaseAuth: FirebaseAuth // Injeta o Auth
) : ViewModel() {

    private val _logs = MutableStateFlow<List<LogGerenciamentoUsuario>>(emptyList())
    val logs = _logs.asStateFlow()

    init {
        carregarLogs()
    }

    private fun carregarLogs() {
        viewModelScope.launch {
            // 1. Descobre qual o companyId do usuário logado
            val email = firebaseAuth.currentUser?.email ?: return@launch
            val usuario = usuarioRepository.buscarPorEmail(email)
            val companyId = usuario?.companyId ?: return@launch

            // 2. Usa a nova função do DAO para buscar e assistir os logs daquela empresa
            db.logGerenciamentoUsuarioDao().listarTodosPorEmpresa(companyId)
                .collectLatest { logsDaEmpresa: List<LogGerenciamentoUsuario> ->
                    _logs.value = logsDaEmpresa
                }
        }
    }
}