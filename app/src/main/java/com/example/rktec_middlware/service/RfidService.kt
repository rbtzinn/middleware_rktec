package com.example.rktec_middleware.service

import com.example.rktec_middleware.model.EpcTag
import kotlinx.coroutines.flow.MutableSharedFlow
import com.pda.rfid.


class RfidService {
    val tagsFlow = MutableSharedFlow<EpcTag>(extraBufferCapacity = 64)

    private var isReading = false
    // Aqui você instancia a SDK (exemplo genérico)
    // private val rfidReader = RfidManager.getInstance()

    fun startReading() {
        if (isReading) return
        isReading = true
        // Iniciar leitura da SDK
        // rfidReader.setOnTagReadListener { epc ->
        //    tagsFlow.tryEmit(EpcTag(epc))
        // }
        // rfidReader.startInventory()
    }

    fun stopReading() {
        if (!isReading) return
        isReading = false
        // rfidReader.stopInventory()
    }

    // Você pode implementar método para liberar a SDK se precisar
}
