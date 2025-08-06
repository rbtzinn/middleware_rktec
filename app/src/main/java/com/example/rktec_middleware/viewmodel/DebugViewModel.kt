package com.example.rktec_middleware.viewmodel

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rktec_middleware.data.model.ItemInventario
import com.example.rktec_middleware.repository.InventarioRepository
import com.example.rktec_middleware.repository.UsuarioRepository
import com.example.rktec_middleware.util.LogHelper
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

    // ##### FUNÇÃO ATUALIZADA #####
    fun atualizarItem(item: ItemInventario) {
        viewModelScope.launch(Dispatchers.IO) {
            // 1. Atualiza no banco local
            inventarioRepository.atualizarItem(item)

            // 2. Tenta enviar para o Firestore
            try {
                inventarioRepository.atualizarItemNoFirestore(item)
                // Se deu certo, mostra um Toast de sucesso no Main thread
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Item sincronizado com a nuvem!", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                // Se deu erro, mostra um Toast de erro no Main thread
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Erro ao sincronizar: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }

            // 3. Recarrega a lista da UI a partir do banco local
            carregarInventario()
        }
    }

    fun limparDadosDaEmpresa() {
        viewModelScope.launch(Dispatchers.IO) {
            val cId = companyId
            val uLogado = usuarioLogado
            if (cId == null || uLogado == null) return@launch

            try {
                inventarioRepository.limparInventarioDoFirestore(cId)
                inventarioRepository.limparJsonDoStorage(cId)
                usuarioRepository.resetarStatusDaEmpresa(cId)
                inventarioRepository.limparInventarioPorEmpresa(cId)

                LogHelper.registrarGerenciamentoUsuario(
                    context = context,
                    usuarioResponsavel = uLogado,
                    acao = "LIMPEZA DE DADOS",
                    usuarioAlvo = "TODA A EMPRESA",
                    motivo = "Reset de inventário via Tela de Debug",
                    detalhes = "Dados de inventário (local, Firestore, Storage) removidos e status da empresa resetado."
                )

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