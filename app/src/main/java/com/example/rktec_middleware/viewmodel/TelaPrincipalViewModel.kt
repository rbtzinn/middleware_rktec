package com.example.rktec_middleware.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rktec_middleware.data.db.AppDatabase
import com.example.rktec_middleware.data.model.ExportProgress
import com.example.rktec_middleware.data.model.SessaoInventario
import com.example.rktec_middleware.data.model.Usuario
import com.example.rktec_middleware.repository.HistoricoRepository
import com.example.rktec_middleware.repository.InventarioRepository
import com.example.rktec_middleware.repository.UsuarioRepository
import com.example.rktec_middleware.util.LogHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

// NOVO: Modelo de dados para agrupar as informações do dashboard
data class DashboardData(
    val totalItensBase: Int = 0,
    val ultimaSessao: SessaoInventario? = null
)

@HiltViewModel
class TelaPrincipalViewModel @Inject constructor(
    private val usuarioRepository: UsuarioRepository,
    private val appDatabase: AppDatabase,
    // NOVAS INJEÇÕES para buscar os dados do dashboard
    private val inventarioRepository: InventarioRepository,
    private val historicoRepository: HistoricoRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _exportState = MutableStateFlow<ExportProgress>(ExportProgress.Idle)
    val exportState: StateFlow<ExportProgress> = _exportState.asStateFlow()

    // NOVO: StateFlow para conter todos os dados do dashboard
    private val _dashboardData = MutableStateFlow(DashboardData())
    val dashboardData: StateFlow<DashboardData> = _dashboardData.asStateFlow()

    init {
        // Carrega os dados do dashboard quando o ViewModel é criado
        carregarDadosDashboard()
    }

    private fun carregarDadosDashboard() {
        viewModelScope.launch {
            // Busca o total de itens em paralelo com a última sessão
            val totalItens = inventarioRepository.listarTodos().size
            historicoRepository.getTodasSessoes().collect { sessoes ->
                _dashboardData.value = DashboardData(
                    totalItensBase = totalItens,
                    ultimaSessao = sessoes.firstOrNull() // Pega a mais recente
                )
            }
        }
    }

    fun exportarPlanilhaCompleta() {
        viewModelScope.launch(Dispatchers.IO) {
            LogHelper.exportarPlanilhaCompleta(context, appDatabase)
                .collect { progress ->
                    _exportState.value = progress
                }
        }
    }

    fun resetExportState() {
        _exportState.value = ExportProgress.Idle
    }
}