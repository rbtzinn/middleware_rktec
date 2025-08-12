package com.example.rktec_middleware

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.KeyEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.IntOffset
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.rktec_middleware.data.db.AppDatabase
import com.example.rktec_middleware.ui.components.AppDrawerContent
import com.example.rktec_middleware.ui.screens.*
import com.example.rktec_middleware.ui.theme.RKTecMiddlewareTheme
import com.example.rktec_middleware.viewmodel.AuthState
import com.example.rktec_middleware.viewmodel.AuthViewModel
import com.example.rktec_middleware.viewmodel.MainViewModel
import com.example.rktec_middleware.viewmodel.RfidViewModel
import com.example.rktec_middleware.ui.components.ConnectivityStatusIndicator
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var appDatabase: AppDatabase
    private val rfidViewModel: RfidViewModel by viewModels()
    private var lendo = false

    @SuppressLint("UnusedBoxWithConstraintsScope")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val mainViewModel: MainViewModel = hiltViewModel()
            val themeOption by mainViewModel.themeOption.collectAsState()
            val connectivityStatus by mainViewModel.connectivityStatus.collectAsState()

            RKTecMiddlewareTheme(themeOption = themeOption) {
                ConnectivityStatusIndicator(status = connectivityStatus)
                val navController = rememberNavController()
                val scope = rememberCoroutineScope()
                val authViewModel: AuthViewModel = hiltViewModel()
                val authState by authViewModel.authState.collectAsState()

                val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

                LaunchedEffect(Unit) {
                    authViewModel.verificarEstadoAutenticacao()
                }

                // Configuração de animações
                val animSpec = tween<IntOffset>(durationMillis = 300)
                fun enterAnim() = slideInHorizontally(initialOffsetX = { it }, animationSpec = animSpec)
                fun exitAnim() = slideOutHorizontally(targetOffsetX = { -it }, animationSpec = animSpec)
                fun popEnterAnim() = slideInHorizontally(initialOffsetX = { -it }, animationSpec = animSpec)
                fun popExitAnim() = slideOutHorizontally(targetOffsetX = { it }, animationSpec = animSpec)

                when (val state = authState) {
                    is AuthState.Carregando -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    }

                    is AuthState.NaoAutenticado, is AuthState.AguardandoVerificacao, is AuthState.Inativo -> {
                        NavHost(navController = navController, startDestination = Screen.Autenticacao.route) {
                            composable(Screen.Autenticacao.route) {
                                when (state) {
                                    is AuthState.NaoAutenticado -> FluxoAutenticacao(aoLoginSucesso = { authViewModel.onLoginSucesso() })
                                    is AuthState.AguardandoVerificacao -> TelaVerificacaoEmail(
                                        email = state.email,
                                        onVoltarParaLogin = { authViewModel.logout() }
                                    )
                                    is AuthState.Inativo -> TelaReativacao(
                                        authViewModel = authViewModel,
                                        onLogoutClick = { authViewModel.logout() }
                                    )
                                    else -> {}
                                }
                            }
                        }
                    }

                    is AuthState.Autenticado -> {
                        val usuarioAutenticado = state.usuario
                        val startDestination = if (state.empresaJaConfigurada) Screen.Principal.route else Screen.Importacao.route
                        var mostrarDialogLogout by remember { mutableStateOf(false) }

                        ModalNavigationDrawer(
                            drawerState = drawerState,
                            gesturesEnabled = drawerState.isOpen,
                            drawerContent = {
                                AppDrawerContent(
                                    usuario = usuarioAutenticado,
                                    onNavigateToProfile = { navController.navigate(Screen.Perfil.route) },
                                    onNavigateToLogAtividades = { navController.navigate(Screen.LogAtividades.route) },
                                    onNavigateToSettings = { navController.navigate(Screen.Configuracoes.route) },
                                    onLogoutClick = {
                                        scope.launch { drawerState.close() }
                                        mostrarDialogLogout = true
                                    },
                                    onCloseDrawer = { scope.launch { drawerState.close() } }
                                )
                            }
                        ) {
                            NavHost(
                                navController = navController,
                                startDestination = startDestination
                            ) {
                                composable(Screen.Principal.route,
                                    enterTransition = { enterAnim() },
                                    exitTransition = { exitAnim() },
                                    popEnterTransition = { popEnterAnim() },
                                    popExitTransition = { popExitAnim() }
                                ) {
                                    TelaPrincipal(
                                        authViewModel = authViewModel,
                                        onMenuClick = { scope.launch { drawerState.open() } },
                                        onInventarioClick = { navController.navigate(Screen.Inventario.route) },
                                        onChecagemClick = { navController.navigate(Screen.Checagem.route) },
                                        onColetaAvulsaClick = { navController.navigate(Screen.ColetaAvulsa.route) },
                                        onSobreClick = { navController.navigate(Screen.Sobre.route) },
                                        onAjudaClick = { navController.navigate(Screen.Ajuda.route) },
                                        onGerenciarUsuariosClick = { navController.navigate(Screen.GerenciamentoUsuarios.route) },
                                        onHistoricoClick = { navController.navigate(Screen.Historico.route) }
                                    )
                                }

                                composable(Screen.Importacao.route,
                                    enterTransition = { enterAnim() },
                                    exitTransition = { exitAnim() },
                                    popEnterTransition = { popEnterAnim() },
                                    popExitTransition = { popExitAnim() }
                                ) {
                                    TelaImportacao(
                                        onConcluido = {
                                            navController.navigate(Screen.AguardandoProcessamento.createRoute(usuarioAutenticado.companyId))
                                        },
                                        usuario = usuarioAutenticado,
                                        onSobreClick = { navController.navigate(Screen.Sobre.route) }
                                    )
                                }

                                composable(Screen.AguardandoProcessamento.route,
                                    arguments = Screen.AguardandoProcessamento.arguments,
                                    enterTransition = { enterAnim() },
                                    exitTransition = { exitAnim() },
                                    popEnterTransition = { popEnterAnim() },
                                    popExitTransition = { popExitAnim() }
                                ) { backStackEntry ->
                                    val companyId = backStackEntry.arguments?.getString("companyId") ?: ""
                                    TelaAguardandoProcessamento(
                                        companyId = companyId,
                                        onProcessamentoConcluido = {
                                            navController.navigate(Screen.Principal.route) {
                                                popUpTo(Screen.Importacao.route) { inclusive = true }
                                            }
                                        }
                                    )
                                }

                                composable(Screen.Configuracoes.route,
                                    enterTransition = { enterAnim() },
                                    exitTransition = { exitAnim() },
                                    popEnterTransition = { popEnterAnim() },
                                    popExitTransition = { popExitAnim() }
                                ) {
                                    TelaConfiguracoes(
                                        authViewModel = authViewModel,
                                        onVoltar = { navController.popBackStack() }
                                    )
                                }

                                composable(Screen.LogAtividades.route,
                                    enterTransition = { enterAnim() },
                                    exitTransition = { exitAnim() },
                                    popEnterTransition = { popEnterAnim() },
                                    popExitTransition = { popExitAnim() }
                                ) {
                                    TelaLogAtividades(onVoltar = { navController.popBackStack() })
                                }

                                composable(Screen.Perfil.route,
                                    enterTransition = { enterAnim() },
                                    exitTransition = { exitAnim() },
                                    popEnterTransition = { popEnterAnim() },
                                    popExitTransition = { popExitAnim() }
                                ) {
                                    TelaPerfil(
                                        onVoltar = { navController.popBackStack() },
                                        authViewModel = authViewModel
                                    )
                                }

                                composable(Screen.GerenciamentoUsuarios.route,
                                    enterTransition = { enterAnim() },
                                    exitTransition = { exitAnim() },
                                    popEnterTransition = { popEnterAnim() },
                                    popExitTransition = { popExitAnim() }
                                ) {
                                    TelaGerenciamentoUsuarios(onVoltar = { navController.popBackStack() })
                                }

                                composable(Screen.Inventario.route,
                                    enterTransition = { enterAnim() },
                                    exitTransition = { exitAnim() },
                                    popEnterTransition = { popEnterAnim() },
                                    popExitTransition = { popExitAnim() }
                                ) {
                                    TelaInventario(
                                        onVoltar = { navController.popBackStack() },
                                        onIniciarLeituraInventario = { loja, setor ->
                                            navController.navigate(Screen.LeituraInventario.createRoute(loja, setor))
                                        },
                                        onSobreClick = { navController.navigate(Screen.Sobre.route) }
                                    )
                                }

                                composable(Screen.Checagem.route,
                                    enterTransition = { enterAnim() },
                                    exitTransition = { exitAnim() },
                                    popEnterTransition = { popEnterAnim() },
                                    popExitTransition = { popExitAnim() }
                                ) {
                                    TelaChecagem(onVoltar = { navController.popBackStack() })
                                }

                                composable(Screen.ColetaAvulsa.route,
                                    enterTransition = { enterAnim() },
                                    exitTransition = { exitAnim() },
                                    popEnterTransition = { popEnterAnim() },
                                    popExitTransition = { popExitAnim() }
                                ) {
                                    TelaLeituraColeta(viewModel = rfidViewModel, onVoltar = { navController.popBackStack() })
                                }

                                composable(Screen.Sobre.route,
                                    enterTransition = { enterAnim() },
                                    exitTransition = { exitAnim() },
                                    popEnterTransition = { popEnterAnim() },
                                    popExitTransition = { popExitAnim() }
                                ) {
                                    TelaSobre(onVoltar = { navController.popBackStack() })
                                }

                                composable(Screen.LeituraInventario.route,
                                    arguments = Screen.LeituraInventario.arguments,
                                    enterTransition = { enterAnim() },
                                    exitTransition = { exitAnim() },
                                    popEnterTransition = { popEnterAnim() },
                                    popExitTransition = { popExitAnim() }
                                ) {
                                    TelaLeituraInventario(
                                        onVoltar = { navController.popBackStack() },
                                        usuario = usuarioAutenticado
                                    )
                                }

                                composable(
                                    Screen.Ajuda.route,
                                    enterTransition = { enterAnim() },
                                    exitTransition = { exitAnim() },
                                    popEnterTransition = { popEnterAnim() },
                                    popExitTransition = { popExitAnim() }
                                ) {
                                    TelaAjuda(onVoltar = { navController.popBackStack() })
                                }

                                composable(Screen.Historico.route,
                                    enterTransition = { enterAnim() },
                                    exitTransition = { exitAnim() },
                                    popEnterTransition = { popEnterAnim() },
                                    popExitTransition = { popExitAnim() }
                                ) {
                                    TelaHistorico(
                                        onVoltar = { navController.popBackStack() },
                                        onSessaoClick = { sessaoId ->
                                            navController.navigate(Screen.DetalheHistorico.createRoute(sessaoId))
                                        }
                                    )
                                }

                                composable(Screen.DetalheHistorico.route,
                                    arguments = Screen.DetalheHistorico.arguments,
                                    enterTransition = { enterAnim() },
                                    exitTransition = { exitAnim() },
                                    popEnterTransition = { popEnterAnim() },
                                    popExitTransition = { popExitAnim() }
                                ) {
                                    TelaDetalheHistorico(onVoltar = { navController.popBackStack() })
                                }
                            }
                        }

                        if (mostrarDialogLogout) {
                            AlertDialog(
                                onDismissRequest = { mostrarDialogLogout = false },
                                icon = { Icon(Icons.Default.ExitToApp, contentDescription = null) },
                                title = { Text("Confirmar Saída") },
                                text = { Text("Tem certeza que deseja sair da sua conta?") },
                                confirmButton = {
                                    Button(
                                        onClick = {
                                            authViewModel.logout()
                                            mostrarDialogLogout = false
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                                    ) { Text("Sair") }
                                },
                                dismissButton = {
                                    TextButton(onClick = { mostrarDialogLogout = false }) { Text("Cancelar") }
                                }
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
