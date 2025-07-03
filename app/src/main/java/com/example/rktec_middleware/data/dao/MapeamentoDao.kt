package com.example.rktec_middleware.data.dao

import androidx.room.*
import com.example.rktec_middleware.data.model.MapeamentoPlanilha

@Dao
interface MapeamentoDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun inserir(mapeamento: MapeamentoPlanilha)

    @Query("SELECT * FROM mapeamento_planilha WHERE usuario = :usuario LIMIT 1")
    suspend fun buscarPorUsuario(usuario: String): MapeamentoPlanilha?

    @Query("SELECT * FROM mapeamento_planilha LIMIT 1")
    suspend fun buscarPrimeiro(): MapeamentoPlanilha? // Se for global

    @Update
    suspend fun atualizar(mapeamento: MapeamentoPlanilha)

    @Delete
    suspend fun deletar(mapeamento: MapeamentoPlanilha)

    @Query("DELETE FROM mapeamento_planilha")
    suspend fun deletarTudo()
}
