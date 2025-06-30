package com.example.rktec_middleware.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rktec_middleware.model.EpcTag
import com.example.rktec_middleware.service.RfidService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class RfidViewModel(
    private val service: RfidService = RfidService()
) : ViewModel() {

    private val _tagList = MutableStateFlow<List<EpcTag>>(emptyList())
    val tagList = _tagList.asStateFlow()

    fun startReading() {
        viewModelScope.launch {
            service.tagsFlow.collect { tag ->
                // Adiciona sem duplicar EPC
                _tagList.value = (_tagList.value + tag).distinctBy { it.epc }
            }
        }
        service.startReading()
    }

    fun stopReading() {
        service.stopReading()
    }
}
