package com.example.rktec_middleware.data.dao

import androidx.room.*
import com.example.rktec_middleware.data.model.LogGerenciamentoUsuario

@Dao
interface LogGerenciamentoUsuarioDao {
    @Insert
    suspend fun inserir(log: LogGerenciamentoUsuario)

    @Query("SELECT * FROM LogGerenciamentoUsuario ORDER BY id DESC")
    suspend fun listarTodos(): List<LogGerenciamentoUsuario>
}
