package com.example.rktec_middleware.viewmodel

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rktec_middleware.data.model.MapeamentoPlanilha
import com.example.rktec_middleware.data.model.Usuario
import com.example.rktec_middleware.repository.InventarioRepository
import com.example.rktec_middleware.repository.UsuarioRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

// Novo estado, mais descritivo para a UI
sealed class MapeamentoState {
    object Idle : MapeamentoState()
    data class Loading(val message: String) : MapeamentoState()
    data class Success(val message: String) : MapeamentoState()
    data class Error(val message: String) : MapeamentoState()
}

@HiltViewModel
class MapeamentoViewModel @Inject constructor(
    private val inventarioRepository: InventarioRepository,
    private val usuarioRepository: UsuarioRepository
) : ViewModel() {

    private val _mapeamentoState = MutableStateFlow<MapeamentoState>(MapeamentoState.Idle)
    val mapeamentoState: StateFlow<MapeamentoState> = _mapeamentoState

    // A ÚNICA FUNÇÃO PÚBLICA QUE A TELA VAI CHAMAR
    fun confirmarMapeamentoEIniciarImportacao(
        usuario: Usuario,
        uri: Uri,
        indexEpc: Int?,
        indexNome: Int?,
        indexSetor: Int?,
        indexLoja: Int?
    ) {
        if (indexEpc == null) {
            _mapeamentoState.value = MapeamentoState.Error("A coluna EPC é obrigatória.")
            return
        }

        viewModelScope.launch {
            try {
                // Passo 1: Enviar o arquivo bruto para o Storage
                _mapeamentoState.value = MapeamentoState.Loading("Enviando arquivo...")
                inventarioRepository.uploadPlanilhaParaStorage(usuario.companyId, uri)

                // Passo 2: Salvar a configuração de mapeamento no Firestore
                _mapeamentoState.value = MapeamentoState.Loading("Salvando configuração de mapeamento...")
                val mapeamento = MapeamentoPlanilha(
                    usuario = usuario.nome,
                    nomeArquivo = uri.lastPathSegment ?: "desconhecido",
                    colunaEpc = indexEpc,
                    colunaNome = indexNome,
                    colunaSetor = indexSetor,
                    colunaLoja = indexLoja
                )
                usuarioRepository.salvarConfiguracaoMapeamento(usuario.companyId, mapeamento)

                // Passo 3: Sucesso!
                _mapeamentoState.value = MapeamentoState.Success("Importação iniciada! O processamento continuará em segundo plano.")

            } catch (e: Exception) {
                Log.e("MapeamentoViewModel", "Falha no processo de importação", e)
                _mapeamentoState.value = MapeamentoState.Error("Erro: ${e.message}")
            }
        }
    }

    fun resetarEstado() {
        _mapeamentoState.value = MapeamentoState.Idle
    }
}