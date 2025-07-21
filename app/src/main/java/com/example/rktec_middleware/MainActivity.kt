package com.example.rktec_middleware

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.KeyEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
// import androidx.compose.ui.graphics.BlendMode.Companion.Screen // IMPORT INCORRETO REMOVIDO
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.rktec_middleware.data.db.AppDatabase
import com.example.rktec_middleware.data.model.TipoUsuario
import com.example.rktec_middleware.data.model.Usuario
import com.example.rktec_middleware.repository.UsuarioRepository
import com.example.rktec_middleware.ui.screens.* // Import correto para a sua classe Screen
import com.example.rktec_middleware.ui.theme.RKTecMiddlewareTheme
import com.example.rktec_middleware.viewmodel.*
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

class MainActivity : ComponentActivity() {
    private lateinit var viewModel: RfidViewModel
    private var lendo = false

    @SuppressLint("UnusedBoxWithConstraintsScope")
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
                                BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                                    val screenWidthPx = with(LocalDensity.current) { constraints.maxWidth.toFloat() }
                                    val offsetX = remember { Animatable(0f) }

                                    val closeDebugScreen: () -> Unit = {
                                        scope.launch {
                                            offsetX.animateTo(0f, tween(300))
                                        }
                                    }

                                    var refreshDebug by remember { mutableStateOf(0) }
                                    TelaDebug(
                                        banco = appDatabase,
                                        refresh = refreshDebug,
                                        usuarioLogado = usuarioAutenticado.nome,
                                        onVoltar = closeDebugScreen,
                                        onBancoLimpo = {
                                            authViewModel.setMapeamentoConcluido(false)
                                            refreshDebug++
                                        }
                                    )

                                    val dragModifier = if (usuarioAutenticado.tipo == TipoUsuario.ADMIN) {
                                        Modifier.pointerInput(Unit) {
                                            detectHorizontalDragGestures(
                                                onHorizontalDrag = { change, dragAmount ->
                                                    change.consume()
                                                    scope.launch {
                                                        val newOffset = (offsetX.value + dragAmount).coerceIn(0f, screenWidthPx)
                                                        offsetX.snapTo(newOffset)
                                                    }
                                                },
                                                onDragEnd = {
                                                    scope.launch {
                                                        if (offsetX.value > screenWidthPx / 2) {
                                                            offsetX.animateTo(screenWidthPx, tween(300))
                                                        } else {
                                                            offsetX.animateTo(0f, tween(300))
                                                        }
                                                    }
                                                }
                                            )
                                        }
                                    } else {
                                        Modifier
                                    }

                                    Box(
                                        modifier = Modifier
                                            .offset { IntOffset(offsetX.value.roundToInt(), 0) }
                                            .then(dragModifier)
                                    ) {
                                        TelaPrincipal(
                                            usuarioDao = appDatabase.usuarioDao(),
                                            usuarioRepository = usuarioRepository,
                                            authViewModel = authViewModel,
                                            onInventarioClick = { navController.navigate(Screen.Inventario.route) },
                                            onChecagemClick = { navController.navigate(Screen.Checagem.route) },
                                            onColetaAvulsaClick = { navController.navigate(Screen.ColetaAvulsa.route) },
                                            onSobreClick = { navController.navigate(Screen.Sobre.route) },
                                            onGerenciarUsuariosClick = { navController.navigate(Screen.GerenciamentoUsuarios.route) },
                                            onSairClick = { authViewModel.logout() }
                                        )
                                    }
                                }
                            }

                            composable(Screen.Checagem.route) {
                                TelaChecagem(onVoltar = { navController.popBackStack() }, banco = appDatabase)
                            }
                            composable(Screen.ColetaAvulsa.route) {
                                TelaLeituraColeta(viewModel = viewModel, onVoltar = { navController.popBackStack() })
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
                                        // CORREÇÃO: Usando a função createRoute para navegar
                                        navController.navigate(Screen.LeituraInventario.createRoute(loja, setor))
                                    },
                                    onSobreClick = { navController.navigate(Screen.Sobre.route) }
                                )
                            }
                            // CORREÇÃO: Usando a rota e os argumentos definidos na classe Screen
                            composable(route = Screen.LeituraInventario.route, arguments = Screen.LeituraInventario.arguments) { backStackEntry ->
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
                            composable(Screen.Sobre.route) {
                                TelaSobre(onVoltar = { navController.popBackStack() })
                            }
                            composable(Screen.GerenciamentoUsuarios.route) {
                                var usuarios by remember { mutableStateOf<List<Usuario>>(emptyList()) }
                                LaunchedEffect(Unit) { usuarios = usuarioRepository.listarTodos() }
                                TelaGerenciamentoUsuarios(
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
