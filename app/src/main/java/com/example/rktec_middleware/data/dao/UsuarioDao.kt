package com.example.rktec_middleware.data.dao

import androidx.room.*
import com.example.rktec_middleware.data.model.Usuario

@Dao
interface UsuarioDao {
    @Insert
    suspend fun inserir(usuario: Usuario)

    @Update
    suspend fun atualizar(usuario: Usuario)

    @Delete
    suspend fun deletar(usuario: Usuario)

    @Query("SELECT * FROM Usuario WHERE email = :email LIMIT 1")
    suspend fun buscarPorEmail(email: String): Usuario?

    @Query("SELECT * FROM Usuario")
    suspend fun listarTodos(): List<Usuario>

    @Query("SELECT * FROM Usuario WHERE nome = :nome LIMIT 1")
    suspend fun buscarPorNome(nome: String): Usuario?
}
