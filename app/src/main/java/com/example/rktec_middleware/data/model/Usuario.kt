package com.example.rktec_middleware.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class TipoUsuario { ADMIN, MEMBRO }

@Entity
data class Usuario(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val nome: String,
    val email: String,
    val senhaHash: String,
    val tipo: TipoUsuario
)
