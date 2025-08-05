package com.example.rktec_middleware.data.model

data class Empresa(
    val id: String = "",
    val nome: String = "",
    val codigoConvite: String = "",
    val planilhaImportada: Boolean = false,
    val inventarioJsonPath: String? = null
)