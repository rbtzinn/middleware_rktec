package com.example.rktec_middleware.service

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import com.example.rktec_middleware.R
import com.example.rktec_middleware.data.model.RfidScanEvent
import com.pda.rfid.EPCModel
import com.pda.rfid.IAsynchronousMessage
import com.pda.rfid.uhf.UHFReader
import com.port.Adapt
import kotlinx.coroutines.flow.MutableSharedFlow

class RfidService(private val context: Context) {
    val tagsFlow = MutableSharedFlow<RfidScanEvent>(extraBufferCapacity = 64)

    private var aberto = false
    private var lendo = false

    private val soundPool: SoundPool
    private var beepSoundId: Int = 0
    private val vibrator: Vibrator

    init {
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
        soundPool = SoundPool.Builder()
            .setMaxStreams(1)
            .setAudioAttributes(audioAttributes)
            .build()

        beepSoundId = soundPool.load(context, R.raw.beep, 1)

        vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
    }

    private val callback = object : IAsynchronousMessage {
        override fun OutPutEPC(model: EPCModel?) {
            if (model?._EPC != null && model._RSSI != null) {
                tagsFlow.tryEmit(RfidScanEvent(epc = model._EPC, rssi = model._RSSI.toString()))
            }
        }
    }

    fun playBeepSound() {
        soundPool.play(beepSoundId, 1f, 1f, 0, 0, 1f)
    }

    fun vibrate() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(50)
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
        soundPool.release()
    }

    fun setPotencia(potencia: Int) {
        UHFReader._Config.SetANTPowerParam(1, potencia)
    }
}