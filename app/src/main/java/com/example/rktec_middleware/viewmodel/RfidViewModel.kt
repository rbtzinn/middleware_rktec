// viewmodel/RfidViewModel.kt
package com.example.rktec_middleware.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rktec_middleware.data.model.EpcTag
import com.example.rktec_middleware.data.model.RfidScanEvent
import com.example.rktec_middleware.service.RfidService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class RfidViewModel(context: Context) : ViewModel() {
    private val service = RfidService(context)
    private val _tagList = MutableStateFlow<List<EpcTag>>(emptyList())
    val tagList: StateFlow<List<EpcTag>> get() = _tagList

    private val _scanEvent = MutableStateFlow<RfidScanEvent?>(null)
    val scanEvent: StateFlow<RfidScanEvent?> get() = _scanEvent

    init {
        viewModelScope.launch(Dispatchers.IO) {
            service.inicializarHardware()
        }

        viewModelScope.launch {
            service.tagsFlow.collect { scanEvent ->
                _scanEvent.value = scanEvent

                val newTag = EpcTag(epc = scanEvent.epc)
                if (_tagList.value.none { it.epc == newTag.epc }) {
                    _tagList.value = _tagList.value + newTag
                }
            }
        }
    }

    fun adicionarTagFake() {
        _tagList.value = (_tagList.value + EpcTag("EPC_TESTE_${System.currentTimeMillis()}")).distinctBy { it.epc }
    }


    fun limparTags() {
        _tagList.value = emptyList()
        _scanEvent.value = null
    }

    fun startReading() = service.iniciarLeitura()
    fun stopReading() = service.pararLeitura()
    fun setPotencia(potencia: Int) = service.setPotencia(potencia)
}