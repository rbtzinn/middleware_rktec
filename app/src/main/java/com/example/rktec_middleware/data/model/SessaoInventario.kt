package com.example.rktec_middleware.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class StatusItemSessao { ENCONTRADO, FALTANTE, ADICIONAL_MESMA_LOJA, ADICIONAL_OUTRA_LOJA, ADICIONAL_DESCONHECIDO }

@Entity(tableName = "sessoes_inventario")
data class SessaoInventario(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val dataHora: Long = System.currentTimeMillis(),
    val usuarioResponsavel: String,
    val filtroLoja: String?,
    val filtroSetor: String?,
    val totalEsperado: Int,
    val totalEncontrado: Int,
    val totalFaltante: Int,
    val totalAdicional: Int,
    val companyId: String = ""
)

@Entity(tableName = "itens_sessao")
data class ItemSessao(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sessaoId: Long,
    val epc: String,
    val descricao: String,
    val status: StatusItemSessao
)