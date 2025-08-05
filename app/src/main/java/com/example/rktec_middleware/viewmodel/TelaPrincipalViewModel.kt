package com.example.rktec_middleware.viewmodel

import android.content.Context
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
    }

    private fun carregarDadosDashboard() {
        viewModelScope.launch(Dispatchers.IO) {
            val email = FirebaseAuth.getInstance().currentUser?.email
            if (email != null) {
                val usuario = usuarioRepository.buscarPorEmail(email)
                val companyId = usuario?.companyId

                if (companyId != null) {
                    val empresa = usuarioRepository.buscarEmpresaPorId(companyId)
                    val totalItens = inventarioRepository.listarTodosPorEmpresa(companyId).size

                    // Usando .firstOrNull() diretamente no Flow para pegar o último item
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

    // ##### FUNÇÃO CORRIGIDA ABAIXO #####
    fun exportarPlanilhaCompleta() {
        viewModelScope.launch(Dispatchers.IO) {
            _exportState.value = ExportProgress.InProgress(0) // Inicia o estado de carregamento
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

            // PASSO 1: Buscar o mapeamento no Firestore usando o repositório
            val mapeamento = usuarioRepository.buscarConfiguracaoMapeamento(companyId)
            if (mapeamento == null) {
                _exportState.value = ExportProgress.Error("Configuração de mapeamento da planilha não foi encontrada.")
                return@launch
            }

            // PASSO 2: Chamar o LogHelper com o mapeamento que foi encontrado
            LogHelper.exportarPlanilhaCompleta(
                context = context,
                banco = appDatabase,
                companyId = companyId,
                mapeamento = mapeamento // <-- Passando o parâmetro que faltava
            ).collect { progress ->
                _exportState.value = progress
            }
        }
    }

    fun resetExportState() {
        _exportState.value = ExportProgress.Idle
    }
}