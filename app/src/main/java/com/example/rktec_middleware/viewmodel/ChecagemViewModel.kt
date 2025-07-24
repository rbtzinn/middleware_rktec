package com.example.rktec_middleware.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rktec_middleware.data.model.ItemInventario
import com.example.rktec_middleware.repository.InventarioRepository
import com.example.rktec_middleware.repository.UsuarioRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChecagemViewModel @Inject constructor(
    private val inventarioRepository: InventarioRepository,
    private val usuarioRepository: UsuarioRepository
) : ViewModel() {

    private val _itemDaBase = MutableStateFlow<ItemInventario?>(null)
    val itemDaBase: StateFlow<ItemInventario?> = _itemDaBase

    private val _buscaConcluida = MutableStateFlow(false)
    val buscaConcluida: StateFlow<Boolean> = _buscaConcluida

    fun buscarItemPorTag(epc: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _buscaConcluida.value = false
            val email = FirebaseAuth.getInstance().currentUser?.email
            if (email != null) {
                val usuario = usuarioRepository.buscarPorEmail(email)
                usuario?.companyId?.let { companyId ->
                    _itemDaBase.value = inventarioRepository.buscarPorTag(epc, companyId)
                }
            }
            _buscaConcluida.value = true
        }
    }

    fun resetar() {
        _itemDaBase.value = null
        _buscaConcluida.value = false
    }
}