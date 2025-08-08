package com.example.rktec_middleware.data.dao

import androidx.room.*
import com.example.rktec_middleware.data.model.ItemInventario
import kotlinx.coroutines.flow.Flow

@Dao
interface InventarioDao {

    @Query("DELETE FROM itens_inventario WHERE companyId = :companyId")
    suspend fun limparInventarioPorEmpresa(companyId: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun inserirTodos(itens: List<ItemInventario>)

    @Query("SELECT * FROM itens_inventario WHERE companyId = :companyId")
    suspend fun listarTodosPorEmpresa(companyId: String): List<ItemInventario>

    @Query("SELECT * FROM itens_inventario WHERE tag = :tag AND companyId = :companyId")
    suspend fun buscarPorTag(tag: String, companyId: String): ItemInventario?

    @Query("UPDATE itens_inventario SET localizacao = :novoSetor WHERE tag = :tag AND companyId = :companyId")
    suspend fun corrigirSetor(tag: String, novoSetor: String, companyId: String)

    @Update
    suspend fun atualizarItem(item: ItemInventario)

    @Query("SELECT COUNT(tag) FROM itens_inventario WHERE companyId = :companyId")
    suspend fun contarItensPorEmpresa(companyId: String): Int

    @Query("SELECT * FROM itens_inventario WHERE companyId = :companyId ORDER BY originalRow ASC")
    fun getInventarioPorEmpresaFlow(companyId: String): Flow<List<ItemInventario>>
}