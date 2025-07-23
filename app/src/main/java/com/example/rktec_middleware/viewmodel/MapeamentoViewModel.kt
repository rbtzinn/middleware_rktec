package com.example.rktec_middleware.viewmodel

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rktec_middleware.data.db.AppDatabase
import com.example.rktec_middleware.data.model.ItemInventario
import com.example.rktec_middleware.data.model.MapeamentoPlanilha
import com.example.rktec_middleware.util.LeitorInventario
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import javax.inject.Inject

sealed class MapeamentoState {
    object Idle : MapeamentoState()
    object Loading : MapeamentoState()
    data class Success(val totalItens: Int) : MapeamentoState()
    data class Error(val message: String) : MapeamentoState()
}

@HiltViewModel
class MapeamentoViewModel @Inject constructor(
    private val db: AppDatabase,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _mapeamentoState = MutableStateFlow<MapeamentoState>(MapeamentoState.Idle)
    val mapeamentoState: StateFlow<MapeamentoState> = _mapeamentoState

    fun processarEsalvarDados(
        usuario: String, uri: Uri,
        dadosBrutos: Pair<List<String>, List<List<String>>>?,
        indexEpc: Int?, indexNome: Int?, indexSetor: Int?, indexLoja: Int?
    ) {
        if (indexEpc == null || dadosBrutos == null) {
            _mapeamentoState.value = MapeamentoState.Error("A coluna EPC é obrigatória.")
            return
        }

        _mapeamentoState.value = MapeamentoState.Loading
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val cabecalho = dadosBrutos.first
                val linhas = dadosBrutos.second

                // CORREÇÃO: Construtor do MapeamentoPlanilha na ordem correta
                val mapeamento = MapeamentoPlanilha(
                    usuario = usuario,
                    nomeArquivo = uri.lastPathSegment ?: "desconhecido",
                    colunaEpc = indexEpc, // Int não nulo
                    colunaNome = indexNome,
                    colunaSetor = indexSetor,
                    colunaLoja = indexLoja
                )

                val indicesMapeados = listOfNotNull(indexEpc, indexNome, indexSetor, indexLoja)

                val listaItens = linhas.mapNotNull { linha ->
                    val tag = linha.getOrNull(indexEpc)?.takeIf { it.isNotBlank() } ?: return@mapNotNull null
                    val desc = indexNome?.let { linha.getOrNull(it) } ?: ""
                    val setor = indexSetor?.let { linha.getOrNull(it) } ?: ""
                    val loja = indexLoja?.let { linha.getOrNull(it) } ?: ""
                    val colunasExtras = mutableMapOf<String, String>()
                    cabecalho.forEachIndexed { index, nomeColuna ->
                        if (index !in indicesMapeados) colunasExtras[nomeColuna] = linha.getOrNull(index) ?: ""
                    }
                    ItemInventario(tag, desc, setor, loja, colunasExtras)
                }

                if (listaItens.isEmpty()) {
                    _mapeamentoState.value = MapeamentoState.Error("Nenhum item válido encontrado na planilha.")
                    return@launch
                }

                db.inventarioDao().limparInventario()
                db.inventarioDao().inserirTodos(listaItens)

                val prefs = context.getSharedPreferences("inventario_prefs", Context.MODE_PRIVATE)
                prefs.edit().putString("cabecalho_original", JSONArray(cabecalho).toString()).apply()

                db.mapeamentoDao().deletarTudo()
                db.mapeamentoDao().inserir(mapeamento)

                _mapeamentoState.value = MapeamentoState.Success(listaItens.size)

            } catch (e: Exception) {
                Log.e("MapeamentoViewModel", "Erro na importação", e)
                _mapeamentoState.value = MapeamentoState.Error("Falha na importação: ${e.message}")
            }
        }
    }
}