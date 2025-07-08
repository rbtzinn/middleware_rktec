package com.example.rktec_middleware.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class LogGerenciamentoUsuario(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val usuarioResponsavel: String, // Quem fez a ação
    val dataHora: String,
    val acao: String, // "EDIÇÃO" ou "EXCLUSÃO"
    val usuarioAlvo: String, // Qual usuário foi editado/excluído (email ou nome)
    val motivo: String?, // Se exclusão, pode ser "Desligamento" ou "Outros"
    val detalhes: String // Ex: "Nome antigo: X, novo: Y"
)
