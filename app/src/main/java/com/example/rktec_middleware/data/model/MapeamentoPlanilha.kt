package com.example.rktec_middleware.data.model

import androidx.room.PrimaryKey

// Não é mais uma @Entity do Room, é apenas uma classe de dados.
data class MapeamentoPlanilha(
    @PrimaryKey(autoGenerate = true) val id: Int = 0, // O ID pode ser mantido, não atrapalha.
    val usuario: String = "",
    val nomeArquivo: String = "",
    val colunaEpc: Int,
    val colunaNome: Int? = null,
    val colunaSetor: Int? = null,
    val colunaLoja: Int? = null
)