package com.example.rktec_middleware.repository

import android.net.Uri
import android.util.Log
import com.example.rktec_middleware.data.dao.InventarioDao
import com.example.rktec_middleware.data.model.ItemInventario
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InventarioRepository @Inject constructor(private val inventarioDao: InventarioDao) {

    private val inventarioCollection = Firebase.firestore.collection("inventario")

    suspend fun listarTodosPorEmpresa(companyId: String): List<ItemInventario> =
        inventarioDao.listarTodosPorEmpresa(companyId)

    suspend fun buscarPorTag(tag: String, companyId: String): ItemInventario? =
        inventarioDao.buscarPorTag(tag, companyId)

    suspend fun corrigirSetor(epc: String, novoSetor: String, companyId: String) =
        inventarioDao.corrigirSetor(epc, novoSetor, companyId)

    suspend fun limparInventarioPorEmpresa(companyId: String) {
        inventarioDao.limparInventarioPorEmpresa(companyId)
    }

    suspend fun atualizarItem(item: ItemInventario) {
        inventarioDao.atualizarItem(item)
    }

    // FUNÇÃO DE UPLOAD ATUALIZADA E SIMPLIFICADA
    suspend fun uploadPlanilhaParaStorage(companyId: String, fileUri: Uri): String {
        val fileName = "importacao-${System.currentTimeMillis()}-${fileUri.lastPathSegment?.replace(" ", "_")}"
        val storageRef = Firebase.storage.reference.child("imports/$companyId/$fileName")

        return try {
            storageRef.putFile(fileUri).await()
            storageRef.path
        } catch (e: Exception) {
            Log.e("InventarioRepository", "Falha no upload para o Storage", e)
            throw e
        }
    }

    // ESTA FUNÇÃO NÃO É MAIS USADA NO CLIENTE, O ROBOZINHO (CLOUD FUNCTION) FAZ ISSO
    /*
    suspend fun uploadInventarioParaFirestore(companyId: String, itens: List<ItemInventario>) {
        val batch = Firebase.firestore.batch()
        itens.forEach { item ->
            val docRef = inventarioCollection.document()
            batch.set(docRef, item.copy(companyId = companyId))
        }
        batch.commit().await()
    }
    */

    suspend fun limparInventarioDoFirestore(companyId: String) {
        val snapshot = inventarioCollection.whereEqualTo("companyId", companyId).get().await()
        val batch = Firebase.firestore.batch()
        snapshot.documents.forEach { doc ->
            batch.delete(doc.reference)
        }
        batch.commit().await()
    }

    suspend fun syncInventarioDoFirestore(companyId: String): List<ItemInventario> {
        return try {
            val snapshot = inventarioCollection.whereEqualTo("companyId", companyId).get().await()
            val itens = snapshot.map { it.toObject<ItemInventario>() }
            if (itens.isNotEmpty()) {
                inventarioDao.limparInventarioPorEmpresa(companyId)
                inventarioDao.inserirTodos(itens)
            }
            itens
        } catch (e: Exception) {
            Log.e("InventarioRepository", "Erro ao sincronizar inventário", e)
            emptyList()
        }
    }
}