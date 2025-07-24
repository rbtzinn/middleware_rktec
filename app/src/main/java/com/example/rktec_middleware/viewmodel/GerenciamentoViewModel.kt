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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GerenciamentoViewModel @Inject constructor(
    private val usuarioRepository: UsuarioRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _usuarios = MutableStateFlow<List<Usuario>>(emptyList())
    val usuarios = _usuarios.asStateFlow()

    // NOVO: StateFlow para o nome da empresa
    private val _nomeEmpresa = MutableStateFlow("")
    val nomeEmpresa = _nomeEmpresa.asStateFlow()

    val usuarioLogadoEmail: String = FirebaseAuth.getInstance().currentUser?.email ?: ""

    init {
        sincronizarEcarregarUsuarios()
    }

    private fun sincronizarEcarregarUsuarios() {
        viewModelScope.launch(Dispatchers.IO) {
            val admin = usuarioRepository.buscarPorEmail(usuarioLogadoEmail)
            val companyId = admin?.companyId

            if (!companyId.isNullOrEmpty()) {
                val empresa = usuarioRepository.buscarEmpresaPorId(companyId)
                _nomeEmpresa.value = empresa?.nome ?: "Empresa Desconhecida"

                val usuariosDaNuvem = usuarioRepository.buscarTodosUsuariosNoFirestore(companyId)
                if (usuariosDaNuvem.isNotEmpty()) {
                    usuariosDaNuvem.forEach { usuario ->
                        usuarioRepository.cadastrarUsuario(usuario)
                    }
                }
                _usuarios.value = usuarioRepository.listarTodosPorEmpresa(companyId).sortedBy { it.nome }
            }
        }
    }

    fun atualizarUsuario(usuario: Usuario) {
        viewModelScope.launch(Dispatchers.IO) {
            usuarioRepository.atualizarUsuario(usuario)
            LogUtil.logAcaoGerenciamentoUsuario(
                context = context, usuarioResponsavel = usuarioLogadoEmail, acao = "EDIÇÃO",
                usuarioAlvo = usuario.email,
                detalhes = "Dados do usuário ${usuario.email} foram atualizados."
            )
            // Recarrega e re-sincroniza a lista para garantir consistência.
            sincronizarEcarregarUsuarios()
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
            // Recarrega e re-sincroniza a lista para garantir consistência.
            sincronizarEcarregarUsuarios()
        }
    }
}