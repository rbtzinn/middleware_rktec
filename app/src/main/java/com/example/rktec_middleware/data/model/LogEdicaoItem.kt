package com.example.rktec_middleware.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Entity(tableName = "log_edicao_inventario")
data class LogEdicaoItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val usuarioResponsavel: String,
    val dataHora: String = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(Date()),
    val tagDoItem: String,
    val campoAlterado: String, // "Descrição", "Setor" ou "Loja"
    val valorAntigo: String,
    val valorNovo: String
)