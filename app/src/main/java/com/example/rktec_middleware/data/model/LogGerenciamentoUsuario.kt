package com.example.rktec_middleware.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "LogGerenciamentoUsuario")
data class LogGerenciamentoUsuario(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val companyId: String,
    val usuarioResponsavel: String,
    val dataHora: String,
    val acao: String,
    val usuarioAlvo: String,
    val motivo: String?,
    val detalhes: String
)

