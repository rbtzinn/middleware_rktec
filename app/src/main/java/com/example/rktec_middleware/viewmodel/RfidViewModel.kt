package com.example.rktec_middleware.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rktec_middleware.data.model.EpcTag
import com.example.rktec_middleware.data.model.RfidScanEvent
import com.example.rktec_middleware.repository.SettingsRepository
import com.example.rktec_middleware.service.RfidService
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RfidViewModel @Inject constructor(
    @ApplicationContext context: Context,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val service = RfidService(context)
    private val _tagList = MutableStateFlow<List<EpcTag>>(emptyList())
    val tagList: StateFlow<List<EpcTag>> get() = _tagList

    private val _scanEvent = MutableStateFlow<RfidScanEvent?>(null)
    val scanEvent: StateFlow<RfidScanEvent?> get() = _scanEvent

    // MUDANÇA 1: Variáveis para guardar o estado atual das configurações
    private val _isSoundEnabled = MutableStateFlow(true)
    private val _isVibrationEnabled = MutableStateFlow(true)

    init {
        viewModelScope.launch(Dispatchers.IO) {
            service.inicializarHardware()
            val potenciaSalva = settingsRepository.rfIdPowerFlow.first()
            service.setPotencia(potenciaSalva)
        }

        // MUDANÇA 2: Corrotina para OBSERVAR as configurações em tempo real
        viewModelScope.launch {
            settingsRepository.soundFeedbackFlow.collect { enabled ->
                _isSoundEnabled.value = enabled
            }
        }
        viewModelScope.launch {
            settingsRepository.vibrationFeedbackFlow.collect { enabled ->
                _isVibrationEnabled.value = enabled
            }
        }

        viewModelScope.launch {
            service.tagsFlow.collect { scanEvent ->
                _scanEvent.value = scanEvent

                val newTag = EpcTag(epc = scanEvent.epc)
                if (_tagList.value.none { it.epc == newTag.epc }) {
                    _tagList.value = _tagList.value + newTag

                    if (_isSoundEnabled.value) {
                        service.playBeepSound()
                    }
                    if (_isVibrationEnabled.value) {
                        service.vibrate()
                    }
                }
            }
        }
    }

    fun playBeep() {
        if (_isSoundEnabled.value) {
            service.playBeepSound()
        }
    }

    fun vibrate() {
        if (_isVibrationEnabled.value) {
            service.vibrate()
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