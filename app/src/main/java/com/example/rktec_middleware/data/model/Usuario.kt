package com.example.rktec_middleware.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class TipoUsuario { ADMIN, MEMBRO }

@Entity(tableName = "usuarios")
data class Usuario(
    @PrimaryKey val email: String = "",
    val nome: String = "",
    val senhaHash: String = "",
    val tipo: TipoUsuario = TipoUsuario.MEMBRO,
    val ativo: Boolean = true,
    val companyId: String = ""
)