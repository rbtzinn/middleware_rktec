package com.example.rktec_middleware

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.KeyEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.rktec_middleware.data.db.AppDatabase
import com.example.rktec_middleware.data.model.TipoUsuario
import com.example.rktec_middleware.data.model.Usuario
import com.example.rktec_middleware.repository.UsuarioRepository
import com.example.rktec_middleware.ui.screens.*
import com.example.rktec_middleware.ui.theme.RKTecMiddlewareTheme
import com.example.rktec_middleware.viewmodel.AuthState
import com.example.rktec_middleware.viewmodel.AuthViewModel
import com.example.rktec_middleware.viewmodel.RfidViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.roundToInt

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var appDatabase: AppDatabase
    @Inject
    lateinit var usuarioRepository: UsuarioRepository

    private val rfidViewModel: RfidViewModel by viewModels()
    private var lendo = false

    @SuppressLint("UnusedBoxWithConstraintsScope")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            RKTecMiddlewareTheme {
                val navController = rememberNavController()
                val scope = rememberCoroutineScope()
                val context = LocalContext.current

                val authViewModel: AuthViewModel = hiltViewModel()
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

                                    TelaDebug(
                                        usuarioLogado = usuarioAutenticado.nome,
                                        onVoltar = closeDebugScreen,
                                        onBancoLimpo = {
                                            authViewModel.setMapeamentoConcluido(false)
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
                                        // CORREÇÃO: TelaPrincipal não precisa mais de usuarioDao e usuarioRepository
                                        TelaPrincipal(
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
                                // CORREÇÃO: TelaChecagem não precisa mais do parâmetro 'banco'.
                                TelaChecagem(onVoltar = { navController.popBackStack() })
                            }
                            composable(Screen.ColetaAvulsa.route) {
                                TelaLeituraColeta(viewModel = rfidViewModel, onVoltar = { navController.popBackStack() })
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
                                        navController.navigate(Screen.LeituraInventario.createRoute(loja, setor))
                                    },
                                    onSobreClick = { navController.navigate(Screen.Sobre.route) }
                                )
                            }
                            composable(route = Screen.LeituraInventario.route, arguments = Screen.LeituraInventario.arguments) {
                                // CORREÇÃO: Não passa mais 'banco', 'filtroLoja' e 'filtroSetor'.
                                // O LeituraInventarioViewModel obtém esses dados dos argumentos da rota.
                                TelaLeituraInventario(
                                    onVoltar = { navController.popBackStack() },
                                    usuarioLogado = usuarioAutenticado.nome
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
            rfidViewModel.startReading()
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == 139 && lendo) {
            lendo = false
            rfidViewModel.stopReading()
            return true
        }
        return super.onKeyUp(keyCode, event)
    }
}