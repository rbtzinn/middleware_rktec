// service/RfidService.kt
package com.example.rktec_middleware.service

import android.content.Context
import android.util.Log
import com.example.rktec_middleware.data.model.RfidScanEvent
import com.pda.rfid.IAsynchronousMessage
import com.pda.rfid.EPCModel
import com.pda.rfid.uhf.UHFReader
import com.port.Adapt
import kotlinx.coroutines.flow.MutableSharedFlow

class RfidService(private val context: Context) {
    val tagsFlow = MutableSharedFlow<RfidScanEvent>(extraBufferCapacity = 64)

    private var aberto = false
    private var lendo = false

    private val callback = object : IAsynchronousMessage {
        override fun OutPutEPC(model: EPCModel?) {
            if (model?._EPC != null && model._RSSI != null) {
                // ALTERAÇÃO: Convertemos o RSSI para String, pois ele vem como um número (Byte).
                tagsFlow.tryEmit(RfidScanEvent(epc = model._EPC, rssi = model._RSSI.toString()))
            }
        }
    }

    fun inicializarHardware() {
        try {
            Adapt.init(context)
            UHFReader.getUHFInstance().OpenConnect(callback)
            aberto = true

            UHFReader._Config.SetEPCBaseBandParam(255, 0, 1, 0)
            UHFReader._Config.SetANTPowerParam(1, 20)

        } catch (e: Exception) {
            aberto = false
            Log.e("RfidService", "Falha ao inicializar o leitor de RFID", e)
        }
    }

    fun iniciarLeitura(): Boolean {
        if (aberto && !lendo) {
            lendo = UHFReader._Tag6C.GetEPC(1, 1) == 0
            return lendo
        }
        return false
    }

    fun pararLeitura() {
        if (aberto && lendo) {
            UHFReader.getUHFInstance().Stop()
            lendo = false
        }
    }

    fun fechar() {
        if (aberto) {
            UHFReader.getUHFInstance().CloseConnect()
            aberto = false
            lendo = false
        }
    }

    fun setPotencia(potencia: Int) {
        UHFReader._Config.SetANTPowerParam(1, potencia)
    }
}