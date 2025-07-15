package com.example.rktec_middleware.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.rktec_middleware.data.model.LogEdicaoItem

@Dao
interface LogEdicaoDao {
    @Insert
    suspend fun inserir(log: LogEdicaoItem)

    @Query("SELECT * FROM log_edicao_inventario ORDER BY id DESC")
    suspend fun listarTodos(): List<LogEdicaoItem>

    @Query("DELETE FROM log_edicao_inventario")
    suspend fun limparLogs()
}