package com.example.rktec_middleware

import TelaImportacao
import android.os.Bundle
import android.view.KeyEvent
import android.util.Log
import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModelProvider
import com.example.rktec_middleware.ui.screens.TelaLeituraColeta
import com.example.rktec_middleware.viewmodel.RfidViewModel
import com.example.rktec_middleware.viewmodel.RfidViewModelFactory
import com.example.rktec_middleware.ui.screens.TelaInventario
import com.example.rktec_middleware.ui.screens.TelaLeituraInventario
import com.example.rktec_middleware.data.db.AppDatabase
import com.example.rktec_middleware.data.model.ItemInventario
import com.example.rktec_middleware.ui.screens.TelaDebug
import com.example.rktec_middleware.ui.screens.TelaSobre
import com.example.rktec_middleware.ui.screens.FluxoAutenticacao
import com.example.rktec_middleware.ui.screens.TelaPrincipal
import com.example.rktec_middleware.data.model.Usuario
import com.example.rktec_middleware.repository.UsuarioRepository
import com.example.rktec_middleware.ui.screens.TelaGerenciamentoUsuarios
import com.example.rktec_middleware.util.UsuarioLogadoManager
import com.example.rktec_middleware.viewmodel.LoginViewModel
import com.example.rktec_middleware.viewmodel.RecuperarSenhaViewModel
import com.example.rktec_middleware.viewmodel.CadastroViewModel
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private lateinit var viewModel: RfidViewModel
    private var lendo = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(
            this,
            RfidViewModelFactory(applicationContext)
        ).get(RfidViewModel::class.java)

        val appDatabase = AppDatabase.getInstance(applicationContext)
        val usuarioRepository = UsuarioRepository(appDatabase.usuarioDao())
        val loginViewModel = LoginViewModel()
        val recuperarSenhaViewModel = RecuperarSenhaViewModel()
        val cadastroViewModel = CadastroViewModel()
        setContent {
            val context = LocalContext.current
            val scope = rememberCoroutineScope()

            var usuarioAutenticado by remember { mutableStateOf<Usuario?>(null) }
            var mapeamentoConcluido by remember { mutableStateOf(false) }
            var telaAtual by remember { mutableStateOf("menu") }
            var refreshDebug by remember { mutableStateOf(0) }
            var filtroLoja by remember { mutableStateOf<String?>(null) }
            var filtroSetor by remember { mutableStateOf<String?>(null) }
            var listaTotal by remember { mutableStateOf<List<ItemInventario>>(emptyList()) }
            var listaFiltrada by remember { mutableStateOf<List<ItemInventario>>(emptyList()) }
            var inicializado by remember { mutableStateOf(false) }

            // ------- AUTO LOGIN AO ABRIR O APP (SÓ UMA VEZ) -------
            LaunchedEffect(Unit) {
                Log.d("RKTEC", "INICIANDO AUTOLOGIN")
                val emailSalvo = UsuarioLogadoManager.obterUsuario(context)
                Log.d("RKTEC", "Email salvo nas prefs: $emailSalvo")
                if (!emailSalvo.isNullOrBlank()) {
                    val usuario = usuarioRepository.buscarPorEmail(emailSalvo)
                    Log.d("RKTEC", "Usuário retornado do Room: $usuario")
                    if (usuario != null) {
                        usuarioAutenticado = usuario
                        val mapeamento = appDatabase.mapeamentoDao().buscarPrimeiro()
                        mapeamentoConcluido = mapeamento != null
                    }
                }
            }

            // ------- FLUXO DE LOGIN/CADASTRO --------
            if (usuarioAutenticado == null) {
                FluxoAutenticacao(
                    loginViewModel = loginViewModel,
                    recuperarSenhaViewModel = recuperarSenhaViewModel,
                    cadastroViewModel = cadastroViewModel,
                    aoLoginSucesso = { usuario ->
                        usuarioAutenticado = usuario
                        scope.launch {
                            Log.d("RKTEC", "Salvando login: ${usuario.email}")
                            UsuarioLogadoManager.salvarUsuario(context, usuario.email)
                            val mapeamento = appDatabase.mapeamentoDao().buscarPrimeiro()
                            mapeamentoConcluido = mapeamento != null
                        }
                    }
                )
                return@setContent
            }
            // ----------- FLUXO DE MAPA/IMPORTAÇÃO -------------
            if (!mapeamentoConcluido) {
                TelaImportacao(
                    onConcluido = {
                        mapeamentoConcluido = true
                        refreshDebug++
                        telaAtual = "menu"
                    },
                    appDatabase = appDatabase,
                    usuario = usuarioAutenticado?.nome ?: "",
                    onDebugClick = { telaAtual = "debug" },
                    onSobreClick = { telaAtual = "sobre" }
                )
                return@setContent
            }

            // ----------- MENU E OUTRAS TELAS -----------
            when (telaAtual) {
                "menu" -> TelaPrincipal(
                    onColetaClick = { telaAtual = "leitura" },
                    onInventarioClick = { telaAtual = "inventario" },
                    onDebugClick = { telaAtual = "debug" },
                    onSobreClick = { telaAtual = "sobre" },
                    nomeUsuario = usuarioAutenticado?.nome ?: "",
                    onSairClick = {
                        scope.launch {
                            Log.d("RKTEC", "Logout: limpando email salvo")
                            UsuarioLogadoManager.limparUsuario(context)
                            usuarioAutenticado = null
                            mapeamentoConcluido = false
                            telaAtual = "menu"
                        }
                    },
                    onGerenciarUsuariosClick = { telaAtual = "usuarios" }
                )

                "leitura" -> TelaLeituraColeta(
                    viewModel = viewModel,
                    onVoltar = { telaAtual = "menu" }
                )
                "inventario" -> TelaInventario(
                    onVoltar = { telaAtual = "menu" },
                    onIniciarLeituraInventario = { loja, setor, total, filtrada ->
                        filtroLoja = loja
                        filtroSetor = setor
                        listaTotal = total
                        listaFiltrada = filtrada
                        telaAtual = "leituraInventario"
                    },
                    onDebugClick = { telaAtual = "debug" },
                    onSobreClick = { telaAtual = "sobre" }
                )
                "leituraInventario" -> TelaLeituraInventario(
                    onVoltar = { telaAtual = "menu" },
                    banco = appDatabase,
                    listaFiltrada = listaFiltrada,
                    listaTotal = listaTotal,
                    filtroLoja = filtroLoja,
                    filtroSetor = filtroSetor
                )
                "debug" -> TelaDebug(
                    banco = appDatabase,
                    refresh = refreshDebug,
                    onVoltar = { telaAtual = "menu" },
                    onBancoLimpo = {
                        mapeamentoConcluido = false
                        telaAtual = "menu"
                    }
                )
                "sobre" -> TelaSobre(
                    onVoltar = { telaAtual = "menu" }
                )
                // ----------- GERENCIAMENTO DE USUÁRIOS -----------
                "usuarios" -> {
                    var usuarios by remember { mutableStateOf<List<Usuario>>(emptyList()) }
                    val refreshUsuarios: () -> Unit = {
                        scope.launch {
                            usuarios = usuarioRepository.listarTodos()
                        }
                    }
                    LaunchedEffect(telaAtual) {
                        if (telaAtual == "usuarios") {
                            usuarios = usuarioRepository.listarTodos()
                        }
                    }
                    TelaGerenciamentoUsuarios(
                        usuarios = usuarios,
                        usuarioRepository = usuarioRepository,
                        usuarioLogado = usuarioAutenticado?.nome ?: "",
                        context = context,
                        onAtualizarLista = refreshUsuarios,
                        onVoltar = { telaAtual = "menu" }
                    )
                }
            }
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == 139 && !lendo) {
            lendo = true
            viewModel.startReading()
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == 139 && lendo) {
            lendo = false
            viewModel.stopReading()
            return true
        }
        return super.onKeyUp(keyCode, event)
    }
}
