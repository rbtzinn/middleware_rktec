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
class ChecagemViewModel @Inject constructor(
    private val inventarioRepository: InventarioRepository
) : ViewModel() {

    private val _itemDaBase = MutableStateFlow<ItemInventario?>(null)
    val itemDaBase: StateFlow<ItemInventario?> = _itemDaBase

    private val _buscaConcluida = MutableStateFlow(false)
    val buscaConcluida: StateFlow<Boolean> = _buscaConcluida

    fun buscarItemPorTag(epc: String) {
        viewModelScope.launch {
            _buscaConcluida.value = false
            _itemDaBase.value = inventarioRepository.buscarPorTag(epc)
            _buscaConcluida.value = true
        }
    }

    fun resetar() {
        _itemDaBase.value = null
        _buscaConcluida.value = false
    }
}