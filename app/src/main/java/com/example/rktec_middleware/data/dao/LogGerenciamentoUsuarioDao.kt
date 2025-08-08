package com.example.rktec_middleware.data.dao

import androidx.room.*
import com.example.rktec_middleware.data.model.LogGerenciamentoUsuario
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface LogGerenciamentoUsuarioDao {
    @Insert
    suspend fun inserir(log: LogGerenciamentoUsuario)

    @Query("SELECT * FROM LogGerenciamentoUsuario ORDER BY id DESC")
    suspend fun listarTodos(): List<LogGerenciamentoUsuario>

    @Query("SELECT * FROM LogGerenciamentoUsuario WHERE companyId = :companyId ORDER BY dataHora DESC")
    fun listarTodosPorEmpresa(companyId: String): Flow<List<LogGerenciamentoUsuario>>
}
