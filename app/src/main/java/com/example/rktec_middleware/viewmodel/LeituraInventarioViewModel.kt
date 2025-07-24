package com.example.rktec_middleware.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rktec_middleware.data.model.EpcTag
import com.example.rktec_middleware.data.model.ItemInventario
import com.example.rktec_middleware.data.model.ItemSessao
import com.example.rktec_middleware.data.model.SessaoInventario
import com.example.rktec_middleware.data.model.StatusItemSessao
import com.example.rktec_middleware.data.model.Usuario
import com.example.rktec_middleware.repository.HistoricoRepository
import com.example.rktec_middleware.repository.InventarioRepository
import com.example.rktec_middleware.repository.UsuarioRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.collections.map

private fun normalizarNome(nome: String): String {
    return nome.replace("\"", "").trim().uppercase()
}

@HiltViewModel
class LeituraInventarioViewModel @Inject constructor(
    private val inventarioRepository: InventarioRepository,
    private val historicoRepository: HistoricoRepository,
    private val usuarioRepository: UsuarioRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    val filtroLoja: String? = savedStateHandle.get<String>("filtroLoja")?.takeIf { it != "null" }
    val filtroSetor: String? = savedStateHandle.get<String>("filtroSetor")?.takeIf { it != "null" }

    private val _listaTotal = MutableStateFlow<List<ItemInventario>>(emptyList())
    val listaTotal: StateFlow<List<ItemInventario>> = _listaTotal

    private val _listaFiltrada = MutableStateFlow<List<ItemInventario>>(emptyList())
    val listaFiltrada: StateFlow<List<ItemInventario>> = _listaFiltrada

    private var companyId: String? = null

    init {
        carregarDados()
    }

    private fun carregarDados() {
        viewModelScope.launch(Dispatchers.IO) {
            val email = FirebaseAuth.getInstance().currentUser?.email
            if (email != null) {
                val usuario = usuarioRepository.buscarPorEmail(email)
                companyId = usuario?.companyId
                if (companyId != null) {
                    val todosOsItens = inventarioRepository.listarTodosPorEmpresa(companyId!!)
                    _listaTotal.value = todosOsItens
                    _listaFiltrada.value = todosOsItens.filter { item ->
                        (filtroLoja.isNullOrEmpty() || normalizarNome(item.loja) == filtroLoja) &&
                                (filtroSetor.isNullOrEmpty() || item.localizacao.trim() == filtroSetor)
                    }
                }
            }
        }
    }

    fun corrigirSetor(epc: String, novoSetor: String) {
        viewModelScope.launch(Dispatchers.IO) {
            companyId?.let { id ->
                inventarioRepository.corrigirSetor(epc, novoSetor, id)
            }
        }
    }

    fun finalizarEsalvarSessao(
        usuario: Usuario,
        tagsLidas: List<EpcTag>
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val epcsLidos = tagsLidas.map { it.epc }.toSet()
            val itensEsperados = _listaFiltrada.value
            val todosOsItens = _listaTotal.value

            val itensEncontrados = itensEsperados.filter { it.tag in epcsLidos }
            val itensFaltantes = itensEsperados.filterNot { it.tag in epcsLidos }
            val epcsAdicionais = epcsLidos - itensEsperados.map { it.tag }.toSet()

            val itensSessao = mutableListOf<ItemSessao>()
            itensEncontrados.forEach { itensSessao.add(ItemSessao(sessaoId = 0, epc = it.tag, descricao = it.desc, status = StatusItemSessao.ENCONTRADO)) }
            itensFaltantes.forEach { itensSessao.add(ItemSessao(sessaoId = 0, epc = it.tag, descricao = it.desc, status = StatusItemSessao.FALTANTE)) }

            epcsAdicionais.forEach { epc ->
                val item = todosOsItens.find { it.tag == epc }
                val status = when {
                    item == null -> StatusItemSessao.ADICIONAL_DESCONHECIDO
                    item.loja != filtroLoja -> StatusItemSessao.ADICIONAL_OUTRA_LOJA
                    else -> StatusItemSessao.ADICIONAL_MESMA_LOJA
                }
                itensSessao.add(ItemSessao(sessaoId = 0, epc = epc, descricao = item?.desc ?: "Item desconhecido", status = status))
            }

            val sessao = SessaoInventario(
                usuarioResponsavel = usuario.nome,
                filtroLoja = filtroLoja,
                filtroSetor = filtroSetor,
                totalEsperado = itensEsperados.size,
                totalEncontrado = itensEncontrados.size,
                totalFaltante = itensFaltantes.size,
                totalAdicional = epcsAdicionais.size,
                companyId = usuario.companyId
            )

            historicoRepository.salvarSessaoCompleta(sessao, itensSessao)
        }
    }
}