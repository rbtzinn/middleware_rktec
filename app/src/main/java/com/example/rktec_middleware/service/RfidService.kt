package com.example.rktec_middleware.service

import android.content.Context
import com.example.rktec_middleware.model.EpcTag
import kotlinx.coroutines.flow.MutableSharedFlow
import com.pda.rfid.IAsynchronousMessage
import com.pda.rfid.uhf.UHFReader
import com.pda.rfid.EPCModel
import com.port.Adapt

class RfidService(context: Context) {
    val tagsFlow = MutableSharedFlow<EpcTag>(extraBufferCapacity = 64)

    private var aberto = false
    private var lendo = false

    private val callback = object : IAsynchronousMessage {
        override fun OutPutEPC(model: EPCModel?) {
            model?._EPC?.let { epc ->
                tagsFlow.tryEmit(EpcTag(epc))
            }
        }
    }

    init {
        try {
            Adapt.init(context)
            UHFReader.getUHFInstance().OpenConnect(callback)
            aberto = true
            // Configuração padrão
            UHFReader._Config.SetEPCBaseBandParam(255, 0, 1, 0)
            UHFReader._Config.SetANTPowerParam(1, 20)
        } catch (e: Exception) {
            aberto = false
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
