// EpcTag.kt
package com.example.rktec_middleware.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "coletas")
data class EpcTag(
    @PrimaryKey val epc: String,
    val timestamp: Long = System.currentTimeMillis(),
    val descricao: String = "" // importante!
)

