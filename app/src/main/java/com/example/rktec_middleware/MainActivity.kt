package com.example.rktec_middleware

import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.rktec_middleware.data.db.AppDatabase
import com.example.rktec_middleware.data.model.ItemInventario
import com.example.rktec_middleware.data.model.LogMapeamento
import com.example.rktec_middleware.data.model.Usuario
import com.example.rktec_middleware.repository.UsuarioRepository
import com.example.rktec_middleware.ui.screens.*
import com.example.rktec_middleware.ui.theme.RKTECmiddlewareTheme
import com.example.rktec_middleware.util.UsuarioLogadoManager
import com.example.rktec_middleware.viewmodel.AuthViewModel
import com.example.rktec_middleware.viewmodel.AuthViewModelFactory
import com.example.rktec_middleware.viewmodel.RfidViewModel
import com.example.rktec_middleware.viewmodel.RfidViewModelFactory
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

        setContent {
            RKTECmiddlewareTheme {
                val context = LocalContext.current
                val scope = rememberCoroutineScope()

                val authViewModel: AuthViewModel = viewModel(
                    factory = AuthViewModelFactory(usuarioRepository)
                )
                val usuarioAutenticado by authViewModel.usuarioAutenticado.collectAsState()
                val mapeamentoConcluido by authViewModel.mapeamentoConcluido.collectAsState()

                // 🔥 NOVO: Agora telaAtual só muda por este bloco! 🔥
                var telaAtual by remember { mutableStateOf("login") }
                var refreshDebug by remember { mutableStateOf(0) }
                var filtroLoja by remember { mutableStateOf<String?>(null) }
                var filtroSetor by remember { mutableStateOf<String?>(null) }
                var listaTotal by remember { mutableStateOf<List<ItemInventario>>(emptyList()) }
                var listaFiltrada by remember { mutableStateOf<List<ItemInventario>>(emptyList()) }
                val usuarioDao = AppDatabase.getInstance(context).usuarioDao()

                // --------- AUTOLOGIN (SÓ UMA VEZ) ---------
                LaunchedEffect(Unit) {
                    Log.d("RKTEC", "INICIANDO AUTOLOGIN")
                    authViewModel.autoLogin(context) {
                        appDatabase.mapeamentoDao().buscarPrimeiro() != null
                    }
                }
                // 🔥 NOVO: O ÚNICO responsável por trocar entre login/menu 🔥
                LaunchedEffect(usuarioAutenticado) {
                    if (usuarioAutenticado == null) {
                        telaAtual = "login"
                    } else if (telaAtual == "login") {
                        telaAtual = "menu"
                    }
                }

                // ----------- FLUXO DE TELAS -----------
                when (telaAtual) {
                    "login" -> {
                        FluxoAutenticacao(
                            usuarioRepository = usuarioRepository,
                            aoLoginSucesso = { usuarioNovo ->
                                if (authViewModel.usuarioAutenticado.value?.email != usuarioNovo.email) {
                                    Log.d(
                                        "RKTEC_DEBUG",
                                        "CHAMOU onLoginSucesso com ${usuarioNovo.email}"
                                    )
                                    authViewModel.login(context, usuarioNovo)
                                    scope.launch {
                                        val mapeamento =
                                            appDatabase.mapeamentoDao().buscarPrimeiro()
                                        authViewModel.setMapeamentoConcluido(mapeamento != null)
                                    }
                                    // NÃO seta telaAtual aqui — controle é do LaunchedEffect!
                                } else {
                                    Log.d(
                                        "RKTEC_DEBUG",
                                        "Ignorou login duplicado de ${usuarioNovo.email}"
                                    )
                                }
                            }
                        )
                    }
                    // Mapeamento
                    "menu" -> {
                        if (!mapeamentoConcluido) {
                            TelaImportacao(
                                onConcluido = { nomeArquivo ->
                                    scope.launch {
                                        val usuario =
                                            usuarioAutenticado?.nome ?: usuarioAutenticado?.email
                                            ?: ""
                                        val dataHora = java.time.LocalDateTime.now()
                                            .format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                                        val log = LogMapeamento(
                                            usuario = usuario,
                                            dataHora = dataHora,
                                            arquivo = nomeArquivo
                                        )
                                        appDatabase.logMapeamentoDao().inserir(log)
                                        authViewModel.setMapeamentoConcluido(true)
                                        refreshDebug++
                                        telaAtual = "menu"
                                    }
                                },
                                appDatabase = appDatabase,
                                usuario = usuarioAutenticado?.nome ?: usuarioAutenticado?.email
                                ?: "",
                                onDebugClick = { telaAtual = "debug" },
                                onSobreClick = { telaAtual = "sobre" }
                            )
                        } else {
                            // Tela principal do app
                            TelaPrincipal(
                                usuarioDao = usuarioDao,
                                authViewModel = authViewModel,
                                onColetaClick = { telaAtual = "leitura" },
                                onInventarioClick = { telaAtual = "inventario" },
                                onDebugClick = { telaAtual = "debug" },
                                onSobreClick = { telaAtual = "sobre" },
                                onSairClick = {
                                    scope.launch {
                                        authViewModel.logout(context)
                                        // Aguarda um frame pra garantir limpeza antes de recompor
                                        kotlinx.coroutines.delay(150)
                                        // telaAtual vai pra login pelo LaunchedEffect
                                    }
                                },
                                onGerenciarUsuariosClick = { telaAtual = "usuarios" }
                            )
                        }
                    }

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
                            authViewModel.setMapeamentoConcluido(false)
                            telaAtual = "menu"
                        }
                    )

                    "sobre" -> TelaSobre(
                        onVoltar = { telaAtual = "menu" }
                    )

                    "usuarios" -> {
                        var usuarios by remember { mutableStateOf<List<Usuario>>(emptyList()) }
                        val refreshUsuarios: () -> Unit = {
                            scope.launch { usuarios = usuarioRepository.listarTodos() }
                        }
                        LaunchedEffect(telaAtual) {
                            if (telaAtual == "usuarios") {
                                usuarios = usuarioRepository.listarTodos()
                            }
                        }
                        TelaGerenciamentoUsuarios(
                            usuarios = usuarios,
                            usuarioRepository = usuarioRepository,
                            usuarioLogadoEmail = usuarioAutenticado?.email ?: "",
                            context = context,
                            onAtualizarLista = { usuarioEditadoEmail ->
                                refreshUsuarios()
                                if (usuarioEditadoEmail == usuarioAutenticado?.email) {
                                    authViewModel.recarregarUsuario(usuarioEditadoEmail)
                                }
                            },
                            onVoltar = { telaAtual = "menu" }
                        )
                    }
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
