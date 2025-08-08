package com.example.rktec_middleware.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rktec_middleware.data.model.Usuario
import com.example.rktec_middleware.repository.UsuarioRepository
import com.example.rktec_middleware.util.LogUtil
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GerenciamentoViewModel @Inject constructor(
    private val usuarioRepository: UsuarioRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _usuarios = MutableStateFlow<List<Usuario>>(emptyList())
    val usuarios = _usuarios.asStateFlow()

    private val _nomeEmpresa = MutableStateFlow("")
    val nomeEmpresa = _nomeEmpresa.asStateFlow()

    val usuarioLogadoEmail: String = FirebaseAuth.getInstance().currentUser?.email ?: ""

    init {
        // A inicialização agora é muito mais leve
        carregarDadosIniciaisEIniciarEscutaLocal()
    }

    private fun carregarDadosIniciaisEIniciarEscutaLocal() {
        viewModelScope.launch(Dispatchers.IO) {
            val admin = usuarioRepository.buscarPorEmail(usuarioLogadoEmail)
            val companyId = admin?.companyId

            if (!companyId.isNullOrEmpty()) {
                // Carrega o nome da empresa (sua lógica original)
                val empresa = usuarioRepository.buscarEmpresaPorId(companyId)
                _nomeEmpresa.value = empresa?.nome ?: "Empresa Desconhecida"

                // Inicia o "ouvinte" do banco de dados local (Room)
                usuarioRepository.getUsuariosPorEmpresaFlow(companyId)
                    .collect { listaDeUsuarios ->
                        _usuarios.value = listaDeUsuarios
                    }
            }
        }
    }

    // Suas lógicas de atualizar e alternar atividade continuam intactas
    fun atualizarUsuario(usuarioAtualizado: Usuario) {
        viewModelScope.launch(Dispatchers.IO) {
            // Busca o usuário original para comparar as mudanças
            val usuarioOriginal = usuarioRepository.buscarUsuarioNoFirestore(usuarioAtualizado.email)
            if (usuarioOriginal == null) return@launch

            // Lógica para criar detalhes
            val detalhes = mutableListOf<String>()
            if (usuarioOriginal.nome != usuarioAtualizado.nome) {
                detalhes.add("Nome alterado de '${usuarioOriginal.nome}' para '${usuarioAtualizado.nome}'")
            }
            if (usuarioOriginal.tipo != usuarioAtualizado.tipo) {
                detalhes.add("Tipo alterado de '${usuarioOriginal.tipo}' para '${usuarioAtualizado.tipo}'")
            }

            // Salva a atualização
            usuarioRepository.atualizarUsuario(usuarioAtualizado)

            // Registra o log detalhado
            LogUtil.logAcaoGerenciamentoUsuario(
                context = context,
                companyId = usuarioAtualizado.companyId,
                usuarioResponsavel = usuarioLogadoEmail,
                acao = "EDIÇÃO DE USUÁRIO",
                usuarioAlvo = usuarioAtualizado.email,
                detalhes = if (detalhes.isNotEmpty()) detalhes.joinToString(", ") else "Nenhuma alteração de dados."
            )
        }
    }

    fun alternarAtividadeUsuario(usuario: Usuario, motivo: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val novaAtividade = !usuario.ativo
            val usuarioAtualizado = usuario.copy(ativo = novaAtividade)
            usuarioRepository.atualizarUsuario(usuarioAtualizado)

            // ✅ FIX: Adicionado companyId, obtido do próprio usuário sendo modificado
            LogUtil.logAcaoGerenciamentoUsuario(
                context = context,
                companyId = usuario.companyId,
                usuarioResponsavel = usuarioLogadoEmail,
                acao = if (novaAtividade) "REATIVAÇÃO" else "DESATIVAÇÃO",
                usuarioAlvo = usuario.email,
                motivo = motivo,
                detalhes = "Status de ${usuario.email} alterado para ${if (novaAtividade) "ativo" else "inativo"}."
            )
        }
    }
}
