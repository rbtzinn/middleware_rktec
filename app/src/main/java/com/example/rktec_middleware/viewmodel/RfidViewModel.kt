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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
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

    init {
        viewModelScope.launch(Dispatchers.IO) {
            service.inicializarHardware()
            val potenciaSalva = settingsRepository.rfIdPowerFlow.first()
            service.setPotencia(potenciaSalva)
        }

        // MUDANÇA: O bloco que coleta as tags agora também aciona o feedback
        viewModelScope.launch {
            // Pega os valores mais recentes das configurações
            val isSoundEnabled = settingsRepository.soundFeedbackFlow.first()
            val isVibrationEnabled = settingsRepository.vibrationFeedbackFlow.first()

            service.tagsFlow.collect { scanEvent ->
                _scanEvent.value = scanEvent

                val newTag = EpcTag(epc = scanEvent.epc)
                // O feedback só acontece para tags NOVAS
                if (_tagList.value.none { it.epc == newTag.epc }) {
                    _tagList.value = _tagList.value + newTag

                    // Aciona o feedback com base nas configurações
                    if (isSoundEnabled) {
                        service.playBeepSound()
                    }
                    if (isVibrationEnabled) {
                        service.vibrate()
                    }
                }
            }
        }
    }

    // O resto do arquivo permanece o mesmo...
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