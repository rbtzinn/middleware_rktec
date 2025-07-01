package com.example.rktec_middleware.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rktec_middleware.data.model.EpcTag
import com.example.rktec_middleware.service.RfidService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class RfidViewModel(context: Context) : ViewModel() {
    private val service = RfidService(context)
    private val _tagList = MutableStateFlow<List<EpcTag>>(emptyList())
    val tagList: StateFlow<List<EpcTag>> get() = _tagList

    init {
        viewModelScope.launch {
            service.tagsFlow.collect { tag ->
                _tagList.value = (_tagList.value + tag).distinctBy { it.epc }
            }
        }
    }

    fun adicionarTagFake() {
        _tagList.value = (_tagList.value + EpcTag("EPC_TESTE_${System.currentTimeMillis()}")).distinctBy { it.epc }
    }

    fun limparTags() {
        _tagList.value = emptyList()
    }

    fun startReading() = service.iniciarLeitura()
    fun stopReading() = service.pararLeitura()
    fun setPotencia(potencia: Int) = service.setPotencia(potencia)
}
