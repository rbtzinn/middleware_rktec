package com.example.rktec_middleware.repository

import com.example.rktec_middleware.data.dao.UsuarioDao
import com.example.rktec_middleware.data.model.Usuario
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

// MUDANÇA: Adicionadas as anotações do Hilt para que ele possa ser injetado
@Singleton
class UsuarioRepository @Inject constructor(private val usuarioDao: UsuarioDao) {

    private val firebaseDb = FirebaseFirestore.getInstance()
    private val usuariosCollection = firebaseDb.collection("usuarios")

    suspend fun buscarPorEmail(email: String) = usuarioDao.buscarPorEmail(email)

    suspend fun buscarPorNome(nome: String) = usuarioDao.buscarPorNome(nome)

    suspend fun listarTodos() = usuarioDao.listarTodos()

    suspend fun listarEmails(): List<String> = usuarioDao.listarEmails()

    suspend fun deletarUsuario(usuario: Usuario) {
        usuarioDao.deletar(usuario)
        usuariosCollection.document(usuario.email).delete().await()
    }

    suspend fun setUsuarioAtivo(email: String, ativo: Boolean) {
        usuarioDao.setAtivo(email, ativo)
    }

    suspend fun cadastrarUsuario(usuario: Usuario) {
        usuarioDao.inserir(usuario)
    }

    suspend fun atualizarUsuario(usuario: Usuario) {
        usuarioDao.atualizar(usuario)
        val usuarioMap = mapOf(
            "nome" to usuario.nome,
            "email" to usuario.email,
            "senhaHash" to usuario.senhaHash,
            "tipo" to usuario.tipo.name
        )
        usuariosCollection.document(usuario.email).set(usuarioMap).await()
    }
}