package com.example.rktec_middleware.repository

import android.net.Uri
import android.util.Log
import com.example.rktec_middleware.data.dao.InventarioDao
import com.example.rktec_middleware.data.model.ItemInventario
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
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
    suspend fun temInventarioLocal(companyId: String): Boolean {
        // Conta quantos itens existem no banco local para essa empresa.
        // Se for maior que zero, significa que os dados já foram baixados.
        return inventarioDao.contarItensPorEmpresa(companyId) > 0
    }
    // Função de upload da planilha
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

    // ÚNICA FUNÇÃO DE SINCRONIZAÇÃO: Baixar e processar o JSON
    suspend fun baixarInventarioEArmazenar(companyId: String, jsonPath: String) {
        withContext(Dispatchers.IO) {
            try {
                Log.d("InventarioRepository", "Iniciando download do JSON de: $jsonPath")
                val storageRef = Firebase.storage.reference.child(jsonPath)
                val maxDownloadSize: Long = 20 * 1024 * 1024 // 20MB (aumentado por segurança)
                val bytes = storageRef.getBytes(maxDownloadSize).await()

                val jsonString = bytes.toString(Charset.defaultCharset())
                val listType = object : TypeToken<List<ItemInventario>>() {}.type
                val itens = gson.fromJson<List<ItemInventario>>(jsonString, listType)

                Log.d("InventarioRepository", "Parse de ${itens.size} itens a partir do JSON bem-sucedido.")

                inventarioDao.limparInventarioPorEmpresa(companyId)
                inventarioDao.inserirTodos(itens)
                Log.d("InventarioRepository", "Dados salvos no banco de dados local (Room).")

            } catch (e: Exception) {
                Log.e("InventarioRepository", "Erro ao sincronizar inventário via JSON", e)
                throw e
            }
        }
    }
}