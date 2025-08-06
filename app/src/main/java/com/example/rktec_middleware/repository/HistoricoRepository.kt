package com.example.rktec_middleware.repository

import android.util.Log
import com.example.rktec_middleware.data.dao.HistoricoDao
import com.example.rktec_middleware.data.model.ItemSessao
import com.example.rktec_middleware.data.model.SessaoInventario
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.channels.awaitClose // IMPORT ADICIONADO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow // IMPORT ADICIONADO
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.jvm.java

@Singleton
class HistoricoRepository @Inject constructor(private val historicoDao: HistoricoDao) {

    private val sessoesCollection = Firebase.firestore.collection("sessoes")

    // --- Sua Lógica Original (Intacta) ---
    fun getTodasSessoes(): Flow<List<SessaoInventario>> = historicoDao.getTodasSessoes()
    suspend fun getSessaoPorId(id: Long): SessaoInventario? = historicoDao.getSessaoPorId(id)
    fun getItensDaSessao(id: Long): Flow<List<ItemSessao>> = historicoDao.getItensDaSessao(id)
    fun getSessoesPorEmpresa(companyId: String): Flow<List<SessaoInventario>> = historicoDao.getSessoesPorEmpresa(companyId)

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

    // ##### FUNÇÕES NOVAS ADICIONADAS PARA O VIGIA #####
    suspend fun inserirSessoes(sessoes: List<SessaoInventario>) {
        historicoDao.inserirSessoes(sessoes)
    }

    fun escutarMudancasDeSessoes(companyId: String): Flow<List<SessaoInventario>> = callbackFlow {
        val listener = sessoesCollection
            .whereEqualTo("companyId", companyId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error); return@addSnapshotListener
                }
                if (snapshot != null) {
                    trySend(snapshot.toObjects(SessaoInventario::class.java))
                }
            }
        awaitClose { listener.remove() }
    }
}