package com.example.rktec_middleware.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "mapeamento_planilha")
data class MapeamentoPlanilha(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val usuario: String = "",
    val nomeArquivo: String = "",
    val colunaEpc: Int,
    val colunaNome: Int? = null,
    val colunaSetor: Int? = null,
    val colunaLoja: Int? = null
)
