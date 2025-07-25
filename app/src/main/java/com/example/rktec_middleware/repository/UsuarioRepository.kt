package com.example.rktec_middleware.repository

import android.util.Log
import com.example.rktec_middleware.data.dao.UsuarioDao
import com.example.rktec_middleware.data.model.Empresa
import com.example.rktec_middleware.data.model.MapeamentoPlanilha // IMPORT ADICIONADO
import com.example.rktec_middleware.data.model.TipoUsuario
import com.example.rktec_middleware.data.model.Usuario
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UsuarioRepository @Inject constructor(private val usuarioDao: UsuarioDao) {

    private val firebaseDb = FirebaseFirestore.getInstance()
    private val usuariosCollection = firebaseDb.collection("usuarios")
    private val empresasCollection = firebaseDb.collection("empresas")

    suspend fun buscarPorEmail(email: String) = usuarioDao.buscarPorEmail(email)
    suspend fun listarTodosPorEmpresa(companyId: String) = usuarioDao.listarTodosPorEmpresa(companyId)
    suspend fun listarEmails(): List<String> = usuarioDao.listarEmails()

    suspend fun buscarUsuarioNoFirestore(email: String): Usuario? {
        return try {
            val document = usuariosCollection.document(email).get().await()
            document.toObject<Usuario>()
        } catch (e: Exception) {
            Log.e("UsuarioRepository", "Erro ao buscar usuário no Firestore", e)
            null
        }
    }

    suspend fun buscarEmpresaPorId(companyId: String): Empresa? {
        return try {
            val document = empresasCollection.document(companyId).get().await()
            document.toObject<Empresa>()?.copy(id = document.id)
        } catch (e: Exception) {
            Log.e("UsuarioRepository", "Erro ao buscar empresa por ID", e)
            null
        }
    }

    // NOVA FUNÇÃO PARA SALVAR A CONFIGURAÇÃO DE MAPEAMENTO
    suspend fun salvarConfiguracaoMapeamento(companyId: String, mapeamento: MapeamentoPlanilha) {
        try {
            empresasCollection.document(companyId)
                .collection("config")
                .document("mapeamento")
                .set(mapeamento)
                .await()
        } catch (e: Exception) {
            Log.e("UsuarioRepository", "Erro ao salvar mapeamento", e)
            throw e
        }
    }

    suspend fun buscarTodosUsuariosNoFirestore(companyId: String): List<Usuario> {
        return try {
            val snapshot = usuariosCollection
                .whereEqualTo("companyId", companyId)
                .get()
                .await()
            snapshot.toObjects(Usuario::class.java)
        } catch (e: Exception) {
            Log.e("UsuarioRepository", "Erro ao buscar todos os usuários no Firestore", e)
            emptyList()
        }
    }

    suspend fun cadastrarUsuario(usuario: Usuario) {
        usuarioDao.inserir(usuario)
        val usuarioMap = mapOf(
            "nome" to usuario.nome,
            "email" to usuario.email,
            "senhaHash" to usuario.senhaHash,
            "tipo" to usuario.tipo.name,
            "ativo" to usuario.ativo,
            "companyId" to usuario.companyId
        )
        usuariosCollection.document(usuario.email).set(usuarioMap).await()
    }

    suspend fun atualizarUsuario(usuario: Usuario) {
        usuarioDao.atualizar(usuario)
        val usuarioMap = mapOf(
            "nome" to usuario.nome,
            "email" to usuario.email,
            "senhaHash" to usuario.senhaHash,
            "tipo" to usuario.tipo.name,
            "ativo" to usuario.ativo,
            "companyId" to usuario.companyId
        )
        usuariosCollection.document(usuario.email).set(usuarioMap).await()
    }

    suspend fun reativarETransferirUsuario(usuario: Usuario, novoCodigoConvite: String): Result<Usuario> {
        return try {
            val snapshot = firebaseDb.collection("empresas")
                .whereEqualTo("codigoConvite", novoCodigoConvite.uppercase())
                .get().await()

            if (snapshot.isEmpty) {
                return Result.failure(Exception("Código da empresa inválido."))
            }

            val novoCompanyId = snapshot.documents.first().id
            val usuarioAtualizado = usuario.copy(
                ativo = true,
                companyId = novoCompanyId,
                tipo = TipoUsuario.MEMBRO
            )
            atualizarUsuario(usuarioAtualizado)
            Result.success(usuarioAtualizado)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun marcarEmpresaComoConfigurada(companyId: String) {
        try {
            empresasCollection.document(companyId).update("planilhaImportada", true).await()
        } catch (e: Exception) {
            Log.e("UsuarioRepository", "Erro ao atualizar status da empresa", e)
        }
    }

    fun escutarMudancasUsuario(email: String): Flow<Usuario?> = callbackFlow {
        val listener = usuariosCollection.document(email)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot != null && snapshot.exists()) {
                    trySend(snapshot.toObject<Usuario>()).isSuccess
                } else {
                    trySend(null).isSuccess
                }
            }
        awaitClose { listener.remove() }
    }
}