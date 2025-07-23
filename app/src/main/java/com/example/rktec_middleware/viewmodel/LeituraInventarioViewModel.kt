package com.example.rktec_middleware.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rktec_middleware.data.model.ItemInventario
import com.example.rktec_middleware.repository.InventarioRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

private fun normalizarNome(nome: String): String {
    return nome.replace("\"", "").trim().uppercase()
}

@HiltViewModel
class LeituraInventarioViewModel @Inject constructor(
    private val repository: InventarioRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    val filtroLoja: String? = savedStateHandle.get<String>("filtroLoja")?.takeIf { it != "null" }
    val filtroSetor: String? = savedStateHandle.get<String>("filtroSetor")?.takeIf { it != "null" }

    private val _listaTotal = MutableStateFlow<List<ItemInventario>>(emptyList())
    val listaTotal: StateFlow<List<ItemInventario>> = _listaTotal

    private val _listaFiltrada = MutableStateFlow<List<ItemInventario>>(emptyList())
    val listaFiltrada: StateFlow<List<ItemInventario>> = _listaFiltrada

    init {
        carregarDados()
    }

    private fun carregarDados() {
        viewModelScope.launch(Dispatchers.IO) {
            val todosOsItens = repository.listarTodos()
            _listaTotal.value = todosOsItens
            _listaFiltrada.value = todosOsItens.filter { item ->
                (filtroLoja.isNullOrEmpty() || normalizarNome(item.loja) == filtroLoja) &&
                        (filtroSetor.isNullOrEmpty() || item.localizacao.trim() == filtroSetor)
            }
        }
    }

    fun corrigirSetor(epc: String, novoSetor: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.corrigirSetor(epc, novoSetor)
        }
    }
}