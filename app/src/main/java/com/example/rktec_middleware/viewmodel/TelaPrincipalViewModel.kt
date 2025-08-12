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
import com.example.rktec_middleware.util.ConnectivityObserver
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

data class ChartData(
    val label: String,
    val value: Float
)

@HiltViewModel
class TelaPrincipalViewModel @Inject constructor(
    private val usuarioRepository: UsuarioRepository,
    private val appDatabase: AppDatabase,
    private val inventarioRepository: InventarioRepository,
    private val historicoRepository: HistoricoRepository,
    @ApplicationContext private val context: Context,
    connectivityObserver: ConnectivityObserver
) : ViewModel() {

    private val _exportState = MutableStateFlow<ExportProgress>(ExportProgress.Idle)
    val exportState: StateFlow<ExportProgress> = _exportState.asStateFlow()
    val connectivityStatus: StateFlow<ConnectivityObserver.Status> = connectivityObserver.observe()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ConnectivityObserver.Status.Available
        )

    private val _dashboardData = MutableStateFlow(DashboardData())
    val dashboardData: StateFlow<DashboardData> = _dashboardData.asStateFlow()

    private val _distribuicaoPorSetor = MutableStateFlow<List<ChartData>>(emptyList())
    val distribuicaoPorSetor: StateFlow<List<ChartData>> = _distribuicaoPorSetor.asStateFlow()

    init {
        // A inicialização agora dispara os "vigias" de dados locais.
        iniciarObservadoresDeDadosLocais()
    }

    private fun iniciarObservadoresDeDadosLocais() {
        viewModelScope.launch(Dispatchers.IO) {
            val email = FirebaseAuth.getInstance().currentUser?.email ?: return@launch
            val usuario = usuarioRepository.buscarPorEmail(email) ?: return@launch
            val companyId = usuario.companyId

            // Vigia 1: Observa a lista de inventário (para o gráfico e total de itens)
            inventarioRepository.getInventarioPorEmpresaFlow(companyId)
                .onEach { inventario ->
                    // Prepara os dados do gráfico
                    val contagemPorSetor = inventario
                        .filter { it.localizacao.isNotBlank() }
                        .groupBy { it.localizacao }
                        .map { ChartData(label = it.key, value = it.value.size.toFloat()) }
                        .sortedByDescending { it.value }
                    _distribuicaoPorSetor.value = contagemPorSetor

                    // Atualiza o total de itens no dashboard
                    _dashboardData.update { it.copy(totalItensBase = inventario.size) }
                }
                .launchIn(viewModelScope)

            // Vigia 2: Observa o histórico de sessões (para o card de "Último Inventário")
            historicoRepository.getSessoesPorEmpresa(companyId)
                .onEach { sessoes ->
                    // Atualiza a última sessão no dashboard (pegando a mais recente pela data)
                    _dashboardData.update { it.copy(ultimaSessao = sessoes.maxByOrNull { s -> s.dataHora }) }
                }
                .launchIn(viewModelScope)

            // Carrega o nome da empresa uma única vez
            val empresa = usuarioRepository.buscarEmpresaPorId(companyId)
            _dashboardData.update { it.copy(nomeEmpresa = empresa?.nome ?: "Empresa não encontrada") }
        }
    }

    // --- SUA LÓGICA ORIGINAL DE EXPORTAÇÃO (INTACTA) ---
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

    fun resetExportState() {
        _exportState.value = ExportProgress.Idle
    }
}