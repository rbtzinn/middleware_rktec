package com.example.rktec_middleware.repository

import android.util.Log
import com.example.rktec_middleware.data.dao.HistoricoDao
import com.example.rktec_middleware.data.model.ItemSessao
import com.example.rktec_middleware.data.model.SessaoInventario
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HistoricoRepository @Inject constructor(private val historicoDao: HistoricoDao) {

    // Inicializa a conexão com o Firestore.
    // "sessoes" será o nome da nossa coleção na nuvem.
    private val sessoesCollection = Firebase.firestore.collection("sessoes")

    // Funções que leem do banco local (Room)
    fun getTodasSessoes() = historicoDao.getTodasSessoes()
    suspend fun getSessaoPorId(id: Long) = historicoDao.getSessaoPorId(id)
    fun getItensDaSessao(id: Long) = historicoDao.getItensDaSessao(id)

    // MUDANÇA: Agora salva nos dois lugares!
    suspend fun salvarSessaoCompleta(sessao: SessaoInventario, itens: List<ItemSessao>) {
        // 1. Salva no banco de dados local (Room)
        val sessaoIdLocal = historicoDao.inserirSessao(sessao)
        val itensComId = itens.map { it.copy(sessaoId = sessaoIdLocal) }
        historicoDao.inserirItensSessao(itensComId)

        // 2. Envia uma cópia para o Firestore
        try {
            sessoesCollection.add(sessao).await()
            Log.d("HistoricoRepository", "Sessão salva com sucesso no Firestore!")
        } catch (e: Exception) {
            Log.e("HistoricoRepository", "Erro ao salvar sessão no Firestore", e)
        }
    }

    suspend fun sincronizarSessoesDaNuvem() {
        try {
            val snapshot = sessoesCollection.get().await()
            val sessoesDaNuvem = snapshot.toObjects(SessaoInventario::class.java)
            Log.d("HistoricoRepository", "${sessoesDaNuvem.size} sessões encontradas na nuvem.")
            // Lógica de mesclagem pode ser adicionada aqui no futuro
        } catch (e: Exception) {
            Log.e("HistoricoRepository", "Erro ao sincronizar sessões da nuvem", e)
        }
    }
}