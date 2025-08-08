package com.example.rktec_middleware.viewmodel

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rktec_middleware.data.db.AppDatabase
import com.example.rktec_middleware.data.model.ItemInventario
import com.example.rktec_middleware.data.model.LogEdicaoItem
import com.example.rktec_middleware.repository.InventarioRepository
import com.example.rktec_middleware.repository.UsuarioRepository
import com.example.rktec_middleware.util.LogHelper
import com.example.rktec_middleware.util.LogUtil
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class DebugViewModel @Inject constructor(
    private val inventarioRepository: InventarioRepository,
    private val usuarioRepository: UsuarioRepository,
    private val appDatabase: AppDatabase,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _inventarioCompleto = MutableStateFlow<List<ItemInventario>>(emptyList())
    val inventarioCompleto = _inventarioCompleto.asStateFlow()

    private var companyId: String? = null
    private var usuarioLogado: String? = null

    init {
        carregarInventario()
    }

    fun carregarInventario() {
        viewModelScope.launch(Dispatchers.IO) {
            val email = FirebaseAuth.getInstance().currentUser?.email
            if (email != null) {
                val usuario = usuarioRepository.buscarPorEmail(email)
                companyId = usuario?.companyId
                usuarioLogado = usuario?.nome
                if (companyId != null) {
                    _inventarioCompleto.value = inventarioRepository.listarTodosPorEmpresa(companyId!!)
                }
            }
        }
    }

    fun atualizarItem(itemAtualizado: ItemInventario) {
        viewModelScope.launch(Dispatchers.IO) {
            val uLogado = usuarioLogado ?: "Desconhecido"
            val itemOriginal = inventarioRepository.buscarPorTag(itemAtualizado.tag, itemAtualizado.companyId)
            if (itemOriginal == null) return@launch

            // Registra as edições específicas para o relatório de edições
            compararEregistrarLogDeEdicao(itemOriginal, itemAtualizado)

            // Registra um log geral para a tela de Log de Atividades
            LogUtil.logAcaoGerenciamentoUsuario(
                context = context,
                companyId = itemAtualizado.companyId,
                usuarioResponsavel = uLogado,
                acao = "EDIÇÃO DE ITEM",
                usuarioAlvo = "Tag: ${itemAtualizado.tag}",
                motivo = "Ajuste manual via tela de Debug",
                detalhes = gerarDetalhesDeEdicao(itemOriginal, itemAtualizado)
            )

            // Salva o item (sua lógica original, intacta)
            inventarioRepository.atualizarItem(itemAtualizado)
            try {
                inventarioRepository.atualizarItemNoFirestore(itemAtualizado)
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Item sincronizado!", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) { /* ... */ }

            carregarInventario()
        }
    }

    private fun gerarDetalhesDeEdicao(original: ItemInventario, novo: ItemInventario): String {
        val detalhes = mutableListOf<String>()
        if (original.desc != novo.desc) detalhes.add("Desc: '${original.desc}' -> '${novo.desc}'")
        if (original.localizacao != novo.localizacao) detalhes.add("Setor: '${original.localizacao}' -> '${novo.localizacao}'")
        if (original.loja != novo.loja) detalhes.add("Loja: '${original.loja}' -> '${novo.loja}'")
        return if (detalhes.isNotEmpty()) detalhes.joinToString("; ") else "Nenhuma alteração de dados."
    }

    private suspend fun compararEregistrarLogDeEdicao(original: ItemInventario, novo: ItemInventario) {
        val logDao = appDatabase.logEdicaoDao()
        val responsavel = usuarioLogado ?: "Desconhecido"
        val tag = original.tag

        if (original.desc != novo.desc) {
            logDao.inserir(LogEdicaoItem(
                usuarioResponsavel = responsavel, tagDoItem = tag,
                campoAlterado = "Descrição", valorAntigo = original.desc, valorNovo = novo.desc
            ))
        }
        if (original.localizacao != novo.localizacao) {
            logDao.inserir(LogEdicaoItem(
                usuarioResponsavel = responsavel, tagDoItem = tag,
                campoAlterado = "Setor", valorAntigo = original.localizacao, valorNovo = novo.localizacao
            ))
        }
        if (original.loja != novo.loja) {
            logDao.inserir(LogEdicaoItem(
                usuarioResponsavel = responsavel, tagDoItem = tag,
                campoAlterado = "Loja", valorAntigo = original.loja, valorNovo = novo.loja
            ))
        }
    }

    fun limparDadosDaEmpresa() {
        viewModelScope.launch(Dispatchers.IO) {
            val cId = companyId
            val uLogado = usuarioLogado
            if (cId == null || uLogado == null) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Erro: Informações do usuário não encontradas.", Toast.LENGTH_SHORT).show()
                }
                return@launch
            }

            try {
                // Passo 1: Limpa o inventário de edições em tempo real do Firestore
                inventarioRepository.limparInventarioDoFirestore(cId)

                // Passo 2: Limpa o arquivo JSON principal do Cloud Storage
                inventarioRepository.limparJsonDoStorage(cId)

                // Passo 3: Reseta o status da empresa e apaga o mapeamento no Firestore
                usuarioRepository.resetarStatusDaEmpresa(cId)

                // Passo 4: Limpa o inventário local do celular (Room)
                inventarioRepository.limparInventarioPorEmpresa(cId)

                // Passo 5: Registra a ação no LogHelper (precisamos passar o companyId)
                LogHelper.registrarGerenciamentoUsuario(
                    context = context,
                    companyId = cId, // Passando o ID da empresa para o log
                    usuarioResponsavel = uLogado,
                    acao = "LIMPEZA DE DADOS",
                    usuarioAlvo = "TODA A EMPRESA ($cId)",
                    motivo = "Reset de inventário via Tela de Debug",
                    detalhes = "Todos os dados de inventário (local e nuvem) foram removidos e o status da empresa foi resetado."
                )

                // Passo 6: Recarrega a lista da UI (que agora estará vazia)
                carregarInventario()

                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Dados da empresa limpos com sucesso.", Toast.LENGTH_SHORT).show()
                }

            } catch (e: Exception) {
                Log.e("DebugViewModel", "Falha ao limpar dados da empresa", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Falha ao limpar dados: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}
