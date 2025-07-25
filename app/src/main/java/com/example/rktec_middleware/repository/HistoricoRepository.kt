package com.example.rktec_middleware.repository

import android.util.Log
import com.example.rktec_middleware.data.dao.HistoricoDao
import com.example.rktec_middleware.data.model.ItemSessao
import com.example.rktec_middleware.data.model.SessaoInventario
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HistoricoRepository @Inject constructor(private val historicoDao: HistoricoDao) {

    private val sessoesCollection = Firebase.firestore.collection("sessoes")

    fun getTodasSessoes(): Flow<List<SessaoInventario>> = historicoDao.getTodasSessoes()

    suspend fun getSessaoPorId(id: Long): SessaoInventario? = historicoDao.getSessaoPorId(id)

    fun getItensDaSessao(id: Long): Flow<List<ItemSessao>> = historicoDao.getItensDaSessao(id)

    fun getSessoesPorEmpresa(companyId: String): Flow<List<SessaoInventario>> {
        return historicoDao.getSessoesPorEmpresa(companyId)
    }

    suspend fun salvarSessaoCompleta(sessao: SessaoInventario, itens: List<ItemSessao>) {
        val sessaoIdLocal = historicoDao.inserirSessao(sessao)
        val itensComId = itens.map { it.copy(sessaoId = sessaoIdLocal) }
        historicoDao.inserirItensSessao(itensComId)

        try {
            sessoesCollection.add(sessao).await()
            Log.d("HistoricoRepository", "Sessão salva com sucesso no Firestore!")
        } catch (e: Exception) {
            Log.e("HistoricoRepository", "Erro ao salvar sessão no Firestore", e)
        }
    }

    suspend fun sincronizarSessoesDaNuvem(companyId: String) {
        try {
            val snapshot = sessoesCollection
                .whereEqualTo("companyId", companyId)
                .get().await()

            val sessoesNuvem = snapshot.toObjects(SessaoInventario::class.java)
            if (sessoesNuvem.isNotEmpty()) {
                historicoDao.inserirSessoes(sessoesNuvem)
            }
        } catch (e: Exception) {
            Log.e("HistoricoRepository", "Erro ao sincronizar sessões da empresa: $companyId", e)
        }
    }
}