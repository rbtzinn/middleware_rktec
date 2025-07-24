package com.example.rktec_middleware.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rktec_middleware.data.model.ItemSessao
import com.example.rktec_middleware.data.model.SessaoInventario
import com.example.rktec_middleware.repository.HistoricoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DetalheHistoricoViewModel @Inject constructor(
    private val historicoRepository: HistoricoRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val sessaoId: Long = savedStateHandle.get<Long>("sessaoId")!!

    private val _sessao = MutableStateFlow<SessaoInventario?>(null)
    val sessao: StateFlow<SessaoInventario?> = _sessao.asStateFlow()

    val itensDaSessao: StateFlow<List<ItemSessao>> = historicoRepository.getItensDaSessao(sessaoId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    init {
        viewModelScope.launch {
            _sessao.value = historicoRepository.getSessaoPorId(sessaoId)
        }
    }
}