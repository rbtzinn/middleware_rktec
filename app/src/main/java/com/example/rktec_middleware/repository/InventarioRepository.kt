package com.example.rktec_middleware.repository

import android.net.Uri
import android.util.Log
import com.example.rktec_middleware.data.dao.InventarioDao
import com.example.rktec_middleware.data.model.ItemInventario
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.nio.charset.Charset
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InventarioRepository @Inject constructor(
    private val inventarioDao: InventarioDao,
    private val gson: Gson
) {

    // Funções do banco local (Room)
    suspend fun listarTodosPorEmpresa(companyId: String): List<ItemInventario> = inventarioDao.listarTodosPorEmpresa(companyId)
    suspend fun buscarPorTag(tag: String, companyId: String): ItemInventario? = inventarioDao.buscarPorTag(tag, companyId)
    suspend fun atualizarItem(item: ItemInventario) = inventarioDao.atualizarItem(item)
    suspend fun corrigirSetor(epc: String, novoSetor: String, companyId: String) = inventarioDao.corrigirSetor(epc, novoSetor, companyId)
    suspend fun limparInventarioPorEmpresa(companyId: String) = inventarioDao.limparInventarioPorEmpresa(companyId)
    suspend fun temInventarioLocal(companyId: String): Boolean = inventarioDao.contarItensPorEmpresa(companyId) > 0

    // --- Funções de Interação com o Firebase ---

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

    suspend fun baixarInventarioEArmazenar(companyId: String, jsonPath: String) {
        withContext(Dispatchers.IO) {
            try {
                val storageRef = Firebase.storage.reference.child(jsonPath)
                val maxDownloadSize: Long = 20 * 1024 * 1024
                val bytes = storageRef.getBytes(maxDownloadSize).await()
                val jsonString = bytes.toString(Charset.defaultCharset())
                val listType = object : TypeToken<List<ItemInventario>>() {}.type
                val itens = gson.fromJson<List<ItemInventario>>(jsonString, listType)
                inventarioDao.limparInventarioPorEmpresa(companyId)
                inventarioDao.inserirTodos(itens)
            } catch (e: Exception) {
                Log.e("InventarioRepository", "Erro ao sincronizar via JSON", e)
                throw e
            }
        }
    }

    suspend fun atualizarItemNoFirestore(item: ItemInventario) {
        try {
            Firebase.firestore.collection("inventario").document(item.tag).set(item).await()
            Log.d("InventarioRepository", "Item ${item.tag} atualizado no Firestore.")
        } catch (e: Exception) {
            Log.e("InventarioRepository", "Erro ao atualizar item no Firestore", e)
            throw e
        }
    }

    fun escutarMudancasDoInventario(companyId: String): Flow<ItemInventario> = callbackFlow {
        val listener = Firebase.firestore.collection("inventario")
            .whereEqualTo("companyId", companyId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error); return@addSnapshotListener
                }
                snapshot?.documentChanges?.forEach { change ->
                    if (change.type == com.google.firebase.firestore.DocumentChange.Type.ADDED ||
                        change.type == com.google.firebase.firestore.DocumentChange.Type.MODIFIED) {
                        trySend(change.document.toObject(ItemInventario::class.java))
                    }
                }
            }
        awaitClose { listener.remove() }
    }

    suspend fun limparInventarioDoFirestore(companyId: String) {
        try {
            val snapshot = Firebase.firestore.collection("inventario")
                .whereEqualTo("companyId", companyId)
                .get().await()
            if (snapshot.isEmpty) return
            val batch = Firebase.firestore.batch()
            snapshot.documents.forEach { doc -> batch.delete(doc.reference) }
            batch.commit().await()
        } catch (e: Exception) {
            Log.e("InventarioRepository", "Erro ao limpar inventário do Firestore", e)
            throw e
        }
    }

    fun getInventarioPorEmpresaFlow(companyId: String): Flow<List<ItemInventario>> {
        return inventarioDao.getInventarioPorEmpresaFlow(companyId)
    }

    suspend fun limparJsonDoStorage(companyId: String) {
        try {
            val empresaDoc = Firebase.firestore.collection("empresas").document(companyId).get().await()
            val jsonPath = empresaDoc.getString("inventarioJsonPath")
            if (jsonPath != null && jsonPath.isNotBlank()) {
                val storageRef = Firebase.storage.reference.child(jsonPath)
                storageRef.delete().await()
            }
        } catch (e: Exception) {
            // Ignora o erro se o arquivo não existir
            if (e !is com.google.firebase.storage.StorageException) {
                throw e
            }
        }
    }
}