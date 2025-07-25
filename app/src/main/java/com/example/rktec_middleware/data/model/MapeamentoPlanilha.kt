package com.example.rktec_middleware.data.model

data class MapeamentoPlanilha(
    val usuario: String = "",
    val nomeArquivo: String = "",
    val colunaEpc: Int? = null,
    val colunaNome: Int? = null,
    val colunaSetor: Int? = null,
    val colunaLoja: Int? = null
)