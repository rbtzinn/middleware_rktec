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
    fun atualizarUsuario(usuario: Usuario) {
        viewModelScope.launch(Dispatchers.IO) {
            usuarioRepository.atualizarUsuario(usuario)
            LogUtil.logAcaoGerenciamentoUsuario(
                context = context, usuarioResponsavel = usuarioLogadoEmail, acao = "EDIÇÃO",
                usuarioAlvo = usuario.email,
                detalhes = "Dados do usuário ${usuario.email} foram atualizados."
            )
        }
    }

    fun alternarAtividadeUsuario(usuario: Usuario, motivo: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val novaAtividade = !usuario.ativo
            val usuarioAtualizado = usuario.copy(ativo = novaAtividade)
            usuarioRepository.atualizarUsuario(usuarioAtualizado)

            LogUtil.logAcaoGerenciamentoUsuario(
                context = context, usuarioResponsavel = usuarioLogadoEmail,
                acao = if (novaAtividade) "REATIVAÇÃO" else "DESATIVAÇÃO",
                usuarioAlvo = usuario.email, motivo = motivo,
                detalhes = "Status de ${usuario.email} alterado para ${if (novaAtividade) "ativo" else "inativo"}."
            )
        }
    }
}