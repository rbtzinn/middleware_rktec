package com.example.rktec_middleware

import android.os.Bundle
import android.view.KeyEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.rktec_middleware.data.db.AppDatabase
import com.example.rktec_middleware.data.model.ItemInventario
import com.example.rktec_middleware.data.model.Usuario
import com.example.rktec_middleware.repository.UsuarioRepository
import com.example.rktec_middleware.ui.screens.*
import com.example.rktec_middleware.ui.theme.RKTecMiddlewareTheme
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
        viewModel = ViewModelProvider(this, RfidViewModelFactory(applicationContext)).get(RfidViewModel::class.java)

        val appDatabase = AppDatabase.getInstance(applicationContext)
        val usuarioRepository = UsuarioRepository(appDatabase.usuarioDao())

        setContent {
            RKTecMiddlewareTheme {
                val navController = rememberNavController()
                val scope = rememberCoroutineScope()
                val context = LocalContext.current

                val authViewModel: AuthViewModel = viewModel(factory = AuthViewModelFactory(usuarioRepository))
                val usuarioAutenticado by authViewModel.usuarioAutenticado.collectAsState()

                var filtroLoja by remember { mutableStateOf<String?>(null) }
                var filtroSetor by remember { mutableStateOf<String?>(null) }
                var listaTotal by remember { mutableStateOf<List<ItemInventario>>(emptyList()) }
                var listaFiltrada by remember { mutableStateOf<List<ItemInventario>>(emptyList()) }
                var refreshDebug by remember { mutableStateOf(0) }

                var isLoading by remember { mutableStateOf(true) }
                var startDestination by remember { mutableStateOf<String?>(null) }

                LaunchedEffect(Unit) {
                    authViewModel.autoLogin(context) {
                        appDatabase.mapeamentoDao().buscarPrimeiro() != null
                    }
                    val user = authViewModel.usuarioAutenticado.value
                    val mapeamentoConcluido = authViewModel.mapeamentoConcluido.value

                    startDestination = if (user == null) {
                        Screen.Autenticacao.route
                    } else if (!mapeamentoConcluido) {
                        Screen.Importacao.route
                    } else {
                        Screen.Principal.route
                    }
                    isLoading = false
                }

                if (isLoading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else {
                    NavHost(navController = navController, startDestination = startDestination!!) {

                        composable(Screen.Autenticacao.route) {
                            FluxoAutenticacao(
                                usuarioRepository = usuarioRepository,
                                aoLoginSucesso = { usuario ->
                                    authViewModel.login(context, usuario)
                                    scope.launch {
                                        val mapeamento = appDatabase.mapeamentoDao().buscarPrimeiro() != null
                                        val proximaTela = if (mapeamento) Screen.Principal.route else Screen.Importacao.route
                                        navController.navigate(proximaTela) { popUpTo(Screen.Autenticacao.route) { inclusive = true } }
                                    }
                                }
                            )
                        }

                        composable(Screen.Importacao.route) {
                            TelaImportacao(
                                onConcluido = {
                                    authViewModel.setMapeamentoConcluido(true)
                                    refreshDebug++
                                    navController.navigate(Screen.Principal.route) { popUpTo(Screen.Importacao.route) { inclusive = true } }
                                },
                                usuario = usuarioAutenticado?.nome ?: "",
                                onSobreClick = { navController.navigate(Screen.Sobre.route) }
                            )
                        }

                        composable(Screen.Principal.route) {
                            TelaPrincipal(
                                usuarioDao = appDatabase.usuarioDao(),
                                authViewModel = authViewModel,
                                usuarioRepository = usuarioRepository,
                                onInventarioClick = { navController.navigate(Screen.Inventario.route) },
                                onDebugClick = { navController.navigate(Screen.Debug.route) },
                                onSobreClick = { navController.navigate(Screen.Sobre.route) },
                                onGerenciarUsuariosClick = { navController.navigate(Screen.GerenciamentoUsuarios.route) },
                                onSairClick = {
                                    scope.launch {
                                        authViewModel.logout(context)
                                        navController.navigate(Screen.Autenticacao.route) { popUpTo(Screen.Principal.route) { inclusive = true } }
                                    }
                                }
                            )
                        }

                        composable(Screen.Inventario.route) {
                            TelaInventario(
                                onVoltar = { navController.popBackStack() },
                                onIniciarLeituraInventario = { loja, setor, total, filtrada ->
                                    filtroLoja = loja
                                    filtroSetor = setor
                                    listaTotal = total
                                    listaFiltrada = filtrada
                                    navController.navigate(Screen.LeituraInventario.route)
                                },
                                onSobreClick = { navController.navigate(Screen.Sobre.route) }
                            )
                        }

                        composable(Screen.LeituraInventario.route) {
                            TelaLeituraInventario(
                                onVoltar = { navController.popBackStack() },
                                banco = appDatabase,
                                usuarioLogado = usuarioAutenticado?.nome ?: "Desconhecido",
                                listaFiltrada = listaFiltrada,
                                listaTotal = listaTotal,
                                filtroLoja = filtroLoja,
                                filtroSetor = filtroSetor
                            )
                        }

                        composable(Screen.Debug.route) {
                            TelaDebug(
                                banco = appDatabase,
                                refresh = refreshDebug,
                                usuarioLogado = usuarioAutenticado?.nome ?: "Desconhecido",
                                onVoltar = { navController.popBackStack() },
                                onBancoLimpo = {
                                    authViewModel.setMapeamentoConcluido(false)
                                    navController.navigate(Screen.Importacao.route) { popUpTo(Screen.Principal.route) { inclusive = true } }
                                }
                            )
                        }

                        // ROTA QUE FALTAVA ADICIONADA AQUI
                        composable(Screen.Sobre.route) {
                            TelaSobre(onVoltar = { navController.popBackStack() })
                        }

                        composable(Screen.GerenciamentoUsuarios.route) {
                            var usuarios by remember { mutableStateOf<List<Usuario>>(emptyList()) }
                            LaunchedEffect(Unit) {
                                usuarios = usuarioRepository.listarTodos()
                            }
                            val usuariosUnicos = remember(usuarios) {
                                usuarios.distinctBy { it.email }
                            }
                            TelaGerenciamentoUsuarios(
                                usuarios = usuariosUnicos,
                                usuarioRepository = usuarioRepository,
                                usuarioLogadoEmail = usuarioAutenticado?.email ?: "",
                                context = context,
                                onAtualizarLista = { usuarioEditadoEmail ->
                                    scope.launch { usuarios = usuarioRepository.listarTodos() }
                                    if (usuarioEditadoEmail == usuarioAutenticado?.email) {
                                        authViewModel.recarregarUsuario(usuarioEditadoEmail)
                                    }
                                },
                                onVoltar = { navController.popBackStack() }
                            )
                        }
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