package com.example.rktec_middleware.repository

import com.example.rktec_middleware.data.dao.UsuarioDao
import com.example.rktec_middleware.data.model.Usuario
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class UsuarioRepository(private val usuarioDao: UsuarioDao) {

    private val firebaseDb = FirebaseFirestore.getInstance()
    private val usuariosCollection = firebaseDb.collection("usuarios")

    suspend fun buscarPorEmail(email: String) = usuarioDao.buscarPorEmail(email) // <-- ADICIONE ESSA LINHA

    suspend fun buscarPorNome(nome: String) = usuarioDao.buscarPorNome(nome)
    suspend fun listarTodos() = usuarioDao.listarTodos()

    /** Remove do Room e do Firestore pelo e-mail como ID do documento! */
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
