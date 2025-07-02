package com.example.rktec_middleware.data.dao

import androidx.room.*
import com.example.rktec_middleware.data.model.ItemInventario

@Dao
interface InventarioDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun inserirTodos(itens: List<ItemInventario>)

    @Query("SELECT * FROM inventario")
    suspend fun listarTodos(): List<ItemInventario>

    @Query("SELECT * FROM inventario WHERE tag = :tag LIMIT 1")
    suspend fun buscarPorTag(tag: String): ItemInventario?

    @Query("DELETE FROM inventario")
    suspend fun limparInventario()
}

