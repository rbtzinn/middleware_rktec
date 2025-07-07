// ColetaDao.kt
package com.example.rktec_middleware.data.dao

import androidx.room.*
import com.example.rktec_middleware.data.model.EpcTag

@Dao
interface ColetaDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun inserirTodos(tags: List<EpcTag>)

    @Query("SELECT * FROM coletas")
    suspend fun listarTodas(): List<EpcTag>

    @Query("DELETE FROM coletas")
    suspend fun limparColetas()

    @Update
    suspend fun atualizarTag(tag: EpcTag)
}

