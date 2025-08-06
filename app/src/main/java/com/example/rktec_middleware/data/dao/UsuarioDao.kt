package com.example.rktec_middleware.data.dao

import androidx.room.*
import com.example.rktec_middleware.data.model.Usuario
import kotlinx.coroutines.flow.Flow

@Dao
interface UsuarioDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun inserir(usuario: Usuario)

    @Update
    suspend fun atualizar(usuario: Usuario)

    @Delete
    suspend fun deletar(usuario: Usuario)

    @Query("SELECT * FROM usuarios WHERE email = :email LIMIT 1")
    suspend fun buscarPorEmail(email: String): Usuario?

    @Query("SELECT * FROM usuarios WHERE companyId = :companyId")
    suspend fun listarTodosPorEmpresa(companyId: String): List<Usuario>

    @Query("SELECT * FROM usuarios WHERE nome = :nome LIMIT 1")
    suspend fun buscarPorNome(nome: String): Usuario?

    @Query("SELECT * FROM usuarios WHERE companyId = :companyId ORDER BY nome ASC")
    fun getUsuariosPorEmpresaFlow(companyId: String): Flow<List<Usuario>>

    @Query("SELECT email FROM usuarios")
    suspend fun listarEmails(): List<String>

    @Query("UPDATE usuarios SET ativo = :ativo WHERE email = :email")
    suspend fun setAtivo(email: String, ativo: Boolean)
}