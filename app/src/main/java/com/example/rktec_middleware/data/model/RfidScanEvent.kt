// data/model/RfidScanEvent.kt
package com.example.rktec_middleware.data.model

// Esta classe representa um único evento de leitura do RFID,
// contendo a etiqueta e a força do sinal (RSSI).
// Não é uma tabela do banco de dados.
data class RfidScanEvent(val epc: String, val rssi: String)