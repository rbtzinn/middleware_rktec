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
import com.example.rktec_middleware.data.model.Usuario
import com.example.rktec_middleware.repository.UsuarioRepository
import com.example.rktec_middleware.ui.screens.*
import com.example.rktec_middleware.ui.theme.RKTecMiddlewareTheme
import com.example.rktec_middleware.viewmodel.*
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
                val authState by authViewModel.authState.collectAsState()

                LaunchedEffect(Unit) {
                    authViewModel.verificarEstadoAutenticacao {
                        appDatabase.mapeamentoDao().buscarPrimeiro() != null
                    }
                }

                when (val state = authState) {
                    is AuthState.Carregando -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    }
                    is AuthState.NaoAutenticado -> {
                        NavHost(navController = navController, startDestination = Screen.Autenticacao.route) {
                            composable(Screen.Autenticacao.route) {
                                FluxoAutenticacao(
                                    usuarioRepository = usuarioRepository,
                                    aoLoginSucesso = { usuario ->
                                        scope.launch {
                                            val mapeamento = appDatabase.mapeamentoDao().buscarPrimeiro() != null
                                            authViewModel.onLoginSucesso(usuario, mapeamento)
                                        }
                                    }
                                )
                            }
                        }
                    }
                    is AuthState.Autenticado -> {
                        val usuarioAutenticado = state.usuario
                        val startDestination = if (state.mapeamentoConcluido) Screen.Principal.route else Screen.Importacao.route

                        NavHost(navController = navController, startDestination = startDestination) {
                            composable(Screen.Principal.route) {
                                TelaPrincipal(
                                    usuarioDao = appDatabase.usuarioDao(),
                                    usuarioRepository = usuarioRepository,
                                    authViewModel = authViewModel,
                                    onInventarioClick = { navController.navigate(Screen.Inventario.route) },
                                    onChecagemClick = { navController.navigate(Screen.Checagem.route) },
                                    onColetaAvulsaClick = {
                                        navController.navigate(Screen.ColetaAvulsa.route)
                                    },
                                    onDebugClick = { navController.navigate(Screen.Debug.route) },
                                    onSobreClick = { navController.navigate(Screen.Sobre.route) },
                                    onGerenciarUsuariosClick = { navController.navigate(Screen.GerenciamentoUsuarios.route) },
                                    onSairClick = {
                                        authViewModel.logout()
                                    }
                                )
                            }

                            composable(Screen.Checagem.route) {
                                TelaChecagem(
                                    onVoltar = { navController.popBackStack() },
                                    banco = appDatabase
                                )
                            }

                            composable(Screen.ColetaAvulsa.route) {
                                TelaLeituraColeta(
                                    viewModel = viewModel,
                                    onVoltar = {
                                        navController.popBackStack()
                                    }
                                )
                            }

                            composable(Screen.Importacao.route) {
                                TelaImportacao(
                                    onConcluido = {
                                        authViewModel.setMapeamentoConcluido(true)
                                        navController.navigate(Screen.Principal.route) { popUpTo(Screen.Importacao.route) { inclusive = true } }
                                    },
                                    usuario = usuarioAutenticado.nome,
                                    onSobreClick = { navController.navigate(Screen.Sobre.route) }
                                )
                            }

                            composable(Screen.Inventario.route) {
                                TelaInventario(
                                    onVoltar = { navController.popBackStack() },
                                    onIniciarLeituraInventario = { loja, setor ->
                                        navController.navigate("${Screen.LeituraInventario.route}/$loja/$setor")
                                    },
                                    onSobreClick = { navController.navigate(Screen.Sobre.route) }
                                )
                            }

                            composable(
                                route = "${Screen.LeituraInventario.route}/{filtroLoja}/{filtroSetor}",
                            ) { backStackEntry ->
                                val loja = backStackEntry.arguments?.getString("filtroLoja")
                                val setor = backStackEntry.arguments?.getString("filtroSetor")
                                TelaLeituraInventario(
                                    onVoltar = { navController.popBackStack() },
                                    banco = appDatabase,
                                    usuarioLogado = usuarioAutenticado.nome,
                                    filtroLoja = if (loja == "null") null else loja,
                                    filtroSetor = if (setor == "null") null else setor
                                )
                            }

                            composable(Screen.Debug.route) {
                                // ALTERAÇÃO: Adicionando de volta a variável de estado que faltava.
                                var refreshDebug by remember { mutableStateOf(0) }
                                TelaDebug(
                                    banco = appDatabase,
                                    // Passando o parâmetro que faltava
                                    refresh = refreshDebug,
                                    usuarioLogado = usuarioAutenticado.nome,
                                    onVoltar = { navController.popBackStack() },
                                    // Passando o parâmetro que faltava
                                    onBancoLimpo = {
                                        authViewModel.setMapeamentoConcluido(false)
                                        refreshDebug++
                                    }
                                )
                            }

                            composable(Screen.Sobre.route) {
                                TelaSobre(onVoltar = { navController.popBackStack() })
                            }

                            composable(Screen.GerenciamentoUsuarios.route) {
                                // ALTERAÇÃO: Adicionando de volta a lógica para buscar os usuários.
                                var usuarios by remember { mutableStateOf<List<Usuario>>(emptyList()) }
                                LaunchedEffect(Unit) {
                                    usuarios = usuarioRepository.listarTodos()
                                }
                                TelaGerenciamentoUsuarios(
                                    // Passando os parâmetros que faltavam
                                    usuarios = usuarios.distinctBy { it.email },
                                    onAtualizarLista = {
                                        scope.launch { usuarios = usuarioRepository.listarTodos() }
                                        authViewModel.recarregarUsuario()
                                    },
                                    usuarioRepository = usuarioRepository,
                                    usuarioLogadoEmail = usuarioAutenticado.email,
                                    context = context,
                                    onVoltar = { navController.popBackStack() }
                                )
                            }
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