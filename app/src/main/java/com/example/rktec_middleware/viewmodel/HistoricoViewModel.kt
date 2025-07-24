package com.example.rktec_middleware.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rktec_middleware.data.model.SessaoInventario
import com.example.rktec_middleware.repository.HistoricoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HistoricoViewModel @Inject constructor(
    private val historicoRepository: HistoricoRepository
) : ViewModel() {

    val sessoes: StateFlow<List<SessaoInventario>> = historicoRepository.getTodasSessoes()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    init {
        viewModelScope.launch {
            historicoRepository.sincronizarSessoesDaNuvem()
        }
    }
}