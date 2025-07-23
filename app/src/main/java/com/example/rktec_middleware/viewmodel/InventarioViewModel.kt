package com.example.rktec_middleware.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rktec_middleware.data.model.ItemInventario
import com.example.rktec_middleware.repository.InventarioRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class InventarioViewModel @Inject constructor(
    private val repository: InventarioRepository
) : ViewModel() {

    private val _dadosImportados = MutableStateFlow<List<ItemInventario>>(emptyList())
    val dadosImportados: StateFlow<List<ItemInventario>> = _dadosImportados

    init {
        carregarDados()
    }

    private fun carregarDados() {
        viewModelScope.launch {
            _dadosImportados.value = repository.listarTodos()
        }
    }
}