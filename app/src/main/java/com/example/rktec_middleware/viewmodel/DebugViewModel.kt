package com.example.rktec_middleware.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rktec_middleware.data.model.ItemInventario
import com.example.rktec_middleware.repository.InventarioRepository
import com.example.rktec_middleware.repository.UsuarioRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DebugViewModel @Inject constructor(
    private val inventarioRepository: InventarioRepository,
    // INJEÇÃO ADICIONADA: Para buscar o usuário logado
    private val usuarioRepository: UsuarioRepository
) : ViewModel() {

    private val _inventarioCompleto = MutableStateFlow<List<ItemInventario>>(emptyList())
    val inventarioCompleto = _inventarioCompleto.asStateFlow()

    // Guarda o companyId do usuário logado para usar nas operações
    private var companyId: String? = null

    init {
        carregarInventario()
    }

    private fun carregarInventario() {
        viewModelScope.launch(Dispatchers.IO) {
            val email = FirebaseAuth.getInstance().currentUser?.email
            if (email != null) {
                val usuario = usuarioRepository.buscarPorEmail(email)
                companyId = usuario?.companyId
                if (companyId != null) {
                    // CORREÇÃO: Carrega apenas os itens da empresa do usuário
                    _inventarioCompleto.value = inventarioRepository.listarTodosPorEmpresa(companyId!!)
                }
            }
        }
    }

    fun atualizarItem(item: ItemInventario) {
        viewModelScope.launch(Dispatchers.IO) {
            // A função de atualizar no DAO já é segura (usa onConflict=REPLACE)
            inventarioRepository.atualizarItem(item)
            carregarInventario() // Recarrega a lista filtrada
        }
    }

    // CORREÇÃO: A função agora limpa apenas os dados da empresa
    fun limparDadosDaEmpresa() {
        viewModelScope.launch(Dispatchers.IO) {
            companyId?.let { id ->
                inventarioRepository.limparInventarioPorEmpresa(id)
                // Limpa também o mapeamento e outros dados se necessário
                // (ex: db.mapeamentoDao().deletarPorEmpresa(id))
                carregarInventario() // Recarrega a lista (que estará vazia)
            }
        }
    }
}