package com.example.rktec_middleware.viewmodel

import android.content.Context
import android.util.Log // IMPORT ADICIONADO para o log de Sincronia
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rktec_middleware.data.db.AppDatabase
import com.example.rktec_middleware.data.model.ExportProgress
import com.example.rktec_middleware.data.model.SessaoInventario
import com.example.rktec_middleware.repository.HistoricoRepository
import com.example.rktec_middleware.repository.InventarioRepository
import com.example.rktec_middleware.repository.UsuarioRepository
import com.example.rktec_middleware.util.LogHelper
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DashboardData(
    val totalItensBase: Int = 0,
    val ultimaSessao: SessaoInventario? = null,
    val nomeEmpresa: String = ""
)

@HiltViewModel
class TelaPrincipalViewModel @Inject constructor(
    private val usuarioRepository: UsuarioRepository,
    private val appDatabase: AppDatabase,
    private val inventarioRepository: InventarioRepository,
    private val historicoRepository: HistoricoRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _exportState = MutableStateFlow<ExportProgress>(ExportProgress.Idle)
    val exportState: StateFlow<ExportProgress> = _exportState.asStateFlow()

    private val _dashboardData = MutableStateFlow(DashboardData())
    val dashboardData: StateFlow<DashboardData> = _dashboardData.asStateFlow()


    init {
        carregarDadosDashboard()
        iniciarEscutaDeAtualizacoes()
    }

    private fun iniciarEscutaDeAtualizacoes() {
        viewModelScope.launch(Dispatchers.IO) {
            val email = FirebaseAuth.getInstance().currentUser?.email ?: return@launch
            val usuario = usuarioRepository.buscarPorEmail(email) ?: return@launch
            val companyId = usuario.companyId

            // Vigia 1: Escuta por mudanças no INVENTÁRIO
            inventarioRepository.escutarMudancasDoInventario(companyId)
                .catch { e -> Log.e("Sync", "Erro ao escutar inventário", e) }
                .collect { itemAtualizado ->
                    Log.d("Sync", "Item '${itemAtualizado.tag}' recebido. Atualizando Room.")
                    inventarioRepository.atualizarItem(itemAtualizado)
                }

            // ##### VIGIA 2 (NOVO): Escuta por mudanças nos USUÁRIOS #####
            usuarioRepository.escutarMudancasDeUsuariosDaEmpresa(companyId)
                .catch { e -> Log.e("Sync", "Erro ao escutar usuários", e) }
                .collect { listaDeUsuariosDaNuvem ->
                    Log.d("Sync", "${listaDeUsuariosDaNuvem.size} usuários recebidos. Atualizando Room.")
                    // Salva todos os usuários recebidos no banco local.
                    // A função 'inserir' já usa OnConflictStrategy.REPLACE, então ela cria ou atualiza.
                    listaDeUsuariosDaNuvem.forEach { usuario ->
                        usuarioRepository.cadastrarUsuario(usuario)
                    }
                }
        }
    }

    // ##### SUA LÓGICA ORIGINAL (INTACTA) #####
    private fun carregarDadosDashboard() {
        viewModelScope.launch(Dispatchers.IO) {
            val email = FirebaseAuth.getInstance().currentUser?.email
            if (email != null) {
                val usuario = usuarioRepository.buscarPorEmail(email)
                val companyId = usuario?.companyId

                if (companyId != null) {
                    val empresa = usuarioRepository.buscarEmpresaPorId(companyId)
                    val totalItens = inventarioRepository.listarTodosPorEmpresa(companyId).size

                    val ultimaSessaoDaEmpresa = historicoRepository.getTodasSessoes()
                        .map { sessoes -> sessoes.filter { it.companyId == companyId }.maxByOrNull { it.dataHora } }
                        .firstOrNull()

                    _dashboardData.value = DashboardData(
                        totalItensBase = totalItens,
                        ultimaSessao = ultimaSessaoDaEmpresa,
                        nomeEmpresa = empresa?.nome ?: "Empresa não encontrada"
                    )
                }
            }
        }
    }

    // ##### SUA LÓGICA ORIGINAL (INTACTA) #####
    fun exportarPlanilhaCompleta() {
        viewModelScope.launch(Dispatchers.IO) {
            _exportState.value = ExportProgress.InProgress(0)
            val email = FirebaseAuth.getInstance().currentUser?.email
            if (email == null) {
                _exportState.value = ExportProgress.Error("Usuário não autenticado.")
                return@launch
            }

            val usuario = usuarioRepository.buscarPorEmail(email)
            val companyId = usuario?.companyId
            if (companyId == null) {
                _exportState.value = ExportProgress.Error("ID da empresa não encontrado para o usuário.")
                return@launch
            }

            val mapeamento = usuarioRepository.buscarConfiguracaoMapeamento(companyId)
            if (mapeamento == null) {
                _exportState.value = ExportProgress.Error("Configuração de mapeamento da planilha não foi encontrada.")
                return@launch
            }

            LogHelper.exportarPlanilhaCompleta(
                context = context,
                banco = appDatabase,
                companyId = companyId,
                mapeamento = mapeamento
            ).collect { progress ->
                _exportState.value = progress
            }
        }
    }

    // ##### SUA LÓGICA ORIGINAL (INTACTA) #####
    fun resetExportState() {
        _exportState.value = ExportProgress.Idle
    }
}