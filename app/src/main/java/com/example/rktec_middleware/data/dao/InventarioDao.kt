package com.example.rktec_middleware.data.dao

import androidx.room.*
import com.example.rktec_middleware.data.model.ItemInventario

@Dao
interface InventarioDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun inserirTodos(items: List<ItemInventario>)

    @Query("DELETE FROM itens_inventario WHERE companyId = :companyId")
    suspend fun limparInventarioPorEmpresa(companyId: String)

    @Query("SELECT * FROM itens_inventario WHERE companyId = :companyId")
    suspend fun listarTodosPorEmpresa(companyId: String): List<ItemInventario>

    @Query("SELECT * FROM itens_inventario WHERE tag = :tag AND companyId = :companyId")
    suspend fun buscarPorTag(tag: String, companyId: String): ItemInventario?

    @Query("UPDATE itens_inventario SET localizacao = :novoSetor WHERE tag = :tag AND companyId = :companyId")
    suspend fun corrigirSetor(tag: String, novoSetor: String, companyId: String)

    @Update
    suspend fun atualizarItem(item: ItemInventario)
}