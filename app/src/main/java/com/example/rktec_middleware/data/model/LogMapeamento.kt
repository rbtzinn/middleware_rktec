package com.example.rktec_middleware.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class LogMapeamento(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val usuario: String,
    val dataHora: String,
    val arquivo: String
)
