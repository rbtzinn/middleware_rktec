package com.example.rktec_middleware.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rktec_middleware.data.model.SessaoInventario
import com.example.rktec_middleware.repository.HistoricoRepository
import com.example.rktec_middleware.repository.UsuarioRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
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
        // Renomeado para refletir a nova lógica mais robusta
        sincronizarEcarregarHistoricoLocal()
    }

    private fun sincronizarEcarregarHistoricoLocal() {
        viewModelScope.launch(Dispatchers.IO) {
            val email = firebaseAuth.currentUser?.email ?: return@launch
            val usuario = usuarioRepository.buscarPorEmail(email)
            val companyId = usuario?.companyId ?: return@launch

            // ##### LÓGICA ATUALIZADA #####
            // Passo 1: Busca a lista mais recente do Firestore e atualiza o Room.
            // Esta é a sua função original, que já funciona perfeitamente para buscar.
            historicoRepository.sincronizarSessoesDaNuvem(companyId)

            // Passo 2: Inicia a escuta reativa no banco de dados local (Room).
            // Agora o Room está garantidamente atualizado com tudo da nuvem.
            historicoRepository.getSessoesPorEmpresa(companyId).collectLatest { sessoesDaEmpresa ->
                _sessoes.value = sessoesDaEmpresa
            }
        }
    }
}