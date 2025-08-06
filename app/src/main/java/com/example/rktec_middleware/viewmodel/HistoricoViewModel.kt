package com.example.rktec_middleware.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rktec_middleware.data.model.SessaoInventario
import com.example.rktec_middleware.repository.HistoricoRepository
import com.example.rktec_middleware.repository.UsuarioRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HistoricoViewModel @Inject constructor(
    private val historicoRepository: HistoricoRepository,
    private val usuarioRepository: UsuarioRepository,
    private val firebaseAuth: FirebaseAuth
) : ViewModel() {

    private val _sessoes = MutableStateFlow<List<SessaoInventario>>(emptyList())
    val sessoes = _sessoes.asStateFlow()

    init {
        // A inicialização agora é muito mais simples e leve
        carregarHistoricoLocal()
    }

    private fun carregarHistoricoLocal() {
        viewModelScope.launch {
            val email = firebaseAuth.currentUser?.email ?: return@launch
            val usuario = usuarioRepository.buscarPorEmail(email)
            val companyId = usuario?.companyId ?: return@launch

            // Agora, ele apenas "assiste" ao banco de dados local (Room).
            // O TelaPrincipalViewModel garante que o Room esteja sempre sincronizado.
            historicoRepository.getSessoesPorEmpresa(companyId).collectLatest { sessoesDaEmpresa ->
                _sessoes.value = sessoesDaEmpresa
            }
        }
    }
}