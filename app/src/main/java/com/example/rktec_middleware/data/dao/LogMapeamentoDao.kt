package com.example.rktec_middleware.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.rktec_middleware.data.model.LogMapeamento

@Dao
interface LogMapeamentoDao {
    @Insert
    suspend fun inserir(log: LogMapeamento)

    @Query("SELECT * FROM LogMapeamento ORDER BY dataHora DESC")
    suspend fun listarTodos(): List<LogMapeamento>
}