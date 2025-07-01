package com.example.rktec_middleware.data.model

data class EpcTag(
    val epc: String,
    val timestamp: Long = System.currentTimeMillis()
)
