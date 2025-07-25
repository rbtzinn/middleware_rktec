package com.example.rktec_middleware.data.dao

import androidx.room.*
import com.example.rktec_middleware.data.model.ItemSessao
import com.example.rktec_middleware.data.model.SessaoInventario
import kotlinx.coroutines.flow.Flow

@Dao
interface HistoricoDao {
    @Insert
    suspend fun inserirSessao(sessao: SessaoInventario): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun inserirSessoes(sessoes: List<SessaoInventario>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun inserirItensSessao(items: List<ItemSessao>)

    @Query("SELECT * FROM sessoes_inventario ORDER BY dataHora DESC")
    fun getTodasSessoes(): Flow<List<SessaoInventario>>

    @Query("SELECT * FROM sessoes_inventario WHERE id = :sessaoId")
    suspend fun getSessaoPorId(sessaoId: Long): SessaoInventario?

    @Query("SELECT * FROM itens_sessao WHERE sessaoId = :sessaoId")
    fun getItensDaSessao(sessaoId: Long): Flow<List<ItemSessao>>

    @Query("SELECT * FROM sessoes_inventario WHERE companyId = :companyId ORDER BY dataHora DESC")
    fun getSessoesPorEmpresa(companyId: String): Flow<List<SessaoInventario>>
}