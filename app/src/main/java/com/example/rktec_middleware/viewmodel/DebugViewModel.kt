package com.example.rktec_middleware.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rktec_middleware.data.model.ItemInventario
import com.example.rktec_middleware.repository.InventarioRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DebugViewModel @Inject constructor(
    private val repository: InventarioRepository
) : ViewModel() {

    private val _inventarioCompleto = MutableStateFlow<List<ItemInventario>>(emptyList())
    val inventarioCompleto = _inventarioCompleto.asStateFlow()

    init {
        carregarInventario()
    }

    private fun carregarInventario() {
        viewModelScope.launch {
            _inventarioCompleto.value = repository.listarTodos()
        }
    }

    fun atualizarItem(item: ItemInventario) {
        viewModelScope.launch {
            repository.atualizarItem(item)
            carregarInventario()
        }
    }

    fun limparBanco() {
        viewModelScope.launch {
            repository.limparInventario()
            carregarInventario()
        }
    }
}