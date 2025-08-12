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
    private var companyId: String? = null

    init {
        sincronizarEIniciarEscuta()
    }

    private fun sincronizarEIniciarEscuta() {
        viewModelScope.launch(Dispatchers.IO) {
            val admin = usuarioRepository.buscarPorEmail(usuarioLogadoEmail)
            companyId = admin?.companyId

            val cId = companyId
            if (cId.isNullOrEmpty()) return@launch

            // ----- MUDANÇA PRINCIPAL AQUI -----

            // 1. Começamos a ouvir o banco de dados local IMEDIATAMENTE.
            // A UI será populada com os usuários em cache assim que o ViewModel for criado.
            launch {
                usuarioRepository.getUsuariosPorEmpresaFlow(cId)
                    .collect { listaDeUsuarios ->
                        _usuarios.value = listaDeUsuarios
                    }
            }

            // Carrega o nome da empresa (lógica original intacta)
            val empresa = usuarioRepository.buscarEmpresaPorId(cId)
            _nomeEmpresa.value = empresa?.nome ?: "Empresa Desconhecida"

            // 2. AGORA, tentamos sincronizar com a nuvem em segundo plano.
            // Se estiver offline, o `catch` impedirá que a escuta local seja interrompida.
            try {
                val usuariosDaNuvem = usuarioRepository.buscarTodosUsuariosNoFirestore(cId)
                if (usuariosDaNuvem.isNotEmpty()) {
                    usuariosDaNuvem.forEach { usuario ->
                        usuarioRepository.cadastrarUsuario(usuario)
                    }
                    // O `collect` que iniciamos acima vai pegar essas atualizações automaticamente.
                }
            } catch (e: Exception) {
                // Quando offline, simplesmente falha em silêncio, pois a UI já tem os dados locais.
                // Você poderia adicionar um Log aqui se quisesse.
                println("Falha ao sincronizar usuários: ${e.message}")
            }
        }
    }

    fun atualizarUsuario(usuario: Usuario) {
        viewModelScope.launch(Dispatchers.IO) {
            usuarioRepository.atualizarUsuario(usuario)
            LogUtil.logAcaoGerenciamentoUsuario(
                context = context,
                companyId = usuario.companyId,
                usuarioResponsavel = usuarioLogadoEmail,
                acao = "EDIÇÃO",
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