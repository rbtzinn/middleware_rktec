package com.example.rktec_middleware.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rktec_middleware.repository.InventarioRepository
import com.example.rktec_middleware.repository.UsuarioRepository
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class StatusProcessamento {
    object Ocioso : StatusProcessamento()
    object Processando : StatusProcessamento()
    object ProntoParaBaixar : StatusProcessamento() // <-- NOVO ESTADO
    object Baixando : StatusProcessamento() // <-- NOVO ESTADO
    object Concluido : StatusProcessamento()
    data class Erro(val mensagem: String) : StatusProcessamento()
}

@HiltViewModel
class ProcessamentoViewModel @Inject constructor(
    private val inventarioRepository: InventarioRepository,
    private val usuarioRepository: UsuarioRepository
) : ViewModel() {

    private val _status = MutableStateFlow<StatusProcessamento>(StatusProcessamento.Ocioso)
    val status: StateFlow<StatusProcessamento> = _status
    private var companyIdCache: String? = null

    fun escutarStatusProcessamento(companyId: String) {
        companyIdCache = companyId // Guarda o ID para usar depois
        val empresaRef = Firebase.firestore.collection("empresas").document(companyId)

        empresaRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                _status.value = StatusProcessamento.Erro(error.message ?: "Erro de conexão")
                return@addSnapshotListener
            }

            if (snapshot != null && snapshot.exists()) {
                when (snapshot.getString("statusProcessamento")) {
                    "INICIANDO", "PROCESSANDO" -> _status.value = StatusProcessamento.Processando
                    "CONCLUIDO" -> _status.value = StatusProcessamento.ProntoParaBaixar
                    else -> { /* Trata erros ou outros status */ }
                }
            }
        }
    }

    // NOVA FUNÇÃO CHAMADA PELO BOTÃO NA TELA
    fun iniciarSincronizacaoLocal() {
        viewModelScope.launch {
            _status.value = StatusProcessamento.Baixando
            try {
                val id = companyIdCache ?: throw IllegalStateException("Company ID não encontrado")
                val empresa = usuarioRepository.buscarEmpresaPorId(id)
                val jsonPath = empresa?.inventarioJsonPath

                if (jsonPath != null && jsonPath.isNotBlank()) {
                    inventarioRepository.baixarInventarioEArmazenar(id, jsonPath)
                    _status.value = StatusProcessamento.Concluido
                } else {
                    _status.value = StatusProcessamento.Erro("Caminho do arquivo de inventário não foi encontrado.")
                }
            } catch (e: Exception) {
                _status.value = StatusProcessamento.Erro("Falha na sincronização: ${e.message}")
            }
        }
    }
}