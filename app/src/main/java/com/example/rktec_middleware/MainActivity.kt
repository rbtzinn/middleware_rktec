package com.example.rktec_middleware

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.KeyEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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

            RKTecMiddlewareTheme(themeOption = themeOption) {
                val navController = rememberNavController()
                val scope = rememberCoroutineScope()
                val authViewModel: AuthViewModel = hiltViewModel()
                val authState by authViewModel.authState.collectAsState()

                val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

                LaunchedEffect(Unit) {
                    authViewModel.verificarEstadoAutenticacao()
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
                                    aoLoginSucesso = { usuario ->
                                        scope.launch {
                                            val inventarioLocal = appDatabase.inventarioDao().listarTodosPorEmpresa(usuario.companyId)
                                            authViewModel.onLoginSucesso(usuario, inventarioLocal.isNotEmpty())
                                        }
                                    }
                                )
                            }
                        }
                    }

                    is AuthState.Autenticado -> {
                        val usuarioAutenticado = state.usuario
                        val startDestination = if (state.empresaJaConfigurada) Screen.Principal.route else Screen.Importacao.route

                        ModalNavigationDrawer(
                            drawerState = drawerState,
                            gesturesEnabled = drawerState.isOpen,
                            drawerContent = {
                                AppDrawerContent(
                                    usuario = usuarioAutenticado,
                                    onNavigateToProfile = {
                                        navController.navigate(Screen.Perfil.route)
                                        scope.launch { drawerState.close() }
                                    },
                                    onNavigateToSettings = {
                                        navController.navigate(Screen.Configuracoes.route)
                                        scope.launch { drawerState.close() }
                                    },
                                    onLogoutClick = {  // CORRIGIDO!
                                        authViewModel.logout()
                                        scope.launch { drawerState.close() }
                                    },
                                    onCloseDrawer = { scope.launch { drawerState.close() } }
                                )
                            }
                        ) {
                            NavHost(navController = navController, startDestination = startDestination) {
                                composable(Screen.Principal.route) {
                                    TelaPrincipal(
                                        authViewModel = authViewModel,  // PARÂMETRO ADICIONADO
                                        onMenuClick = { scope.launch { drawerState.open() } },
                                        onInventarioClick = { navController.navigate(Screen.Inventario.route) },
                                        onChecagemClick = { navController.navigate(Screen.Checagem.route) },
                                        onColetaAvulsaClick = { navController.navigate(Screen.ColetaAvulsa.route) },
                                        onSobreClick = { navController.navigate(Screen.Sobre.route) },
                                        onGerenciarUsuariosClick = { navController.navigate(Screen.GerenciamentoUsuarios.route) },
                                        onHistoricoClick = { navController.navigate(Screen.Historico.route) }
                                    )
                                }

                                composable(Screen.Configuracoes.route) {
                                    TelaConfiguracoes(
                                        authViewModel = authViewModel,
                                        onVoltar = { navController.popBackStack() }
                                    )
                                }

                                composable(Screen.Perfil.route) {
                                    TelaPerfil(
                                        onVoltar = { navController.popBackStack() },
                                        authViewModel = authViewModel // PASSANDO O VIEWMODEL
                                    )
                                }

                                composable(Screen.GerenciamentoUsuarios.route) {
                                    TelaGerenciamentoUsuarios(onVoltar = { navController.popBackStack() })
                                }

                                composable(Screen.Importacao.route) {
                                    TelaImportacao(
                                        onConcluido = {
                                            authViewModel.setEmpresaConfigurada(true)
                                            navController.navigate(Screen.Principal.route) {
                                                popUpTo(Screen.Importacao.route) { inclusive = true }
                                            }
                                        },
                                        usuario = usuarioAutenticado,
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

                                composable(Screen.Checagem.route) {
                                    TelaChecagem(onVoltar = { navController.popBackStack() })
                                }

                                composable(Screen.ColetaAvulsa.route) {
                                    TelaLeituraColeta(viewModel = rfidViewModel, onVoltar = { navController.popBackStack() })
                                }

                                composable(Screen.Sobre.route) {
                                    TelaSobre(onVoltar = { navController.popBackStack() })
                                }

                                composable(route = Screen.LeituraInventario.route, arguments = Screen.LeituraInventario.arguments) {
                                    TelaLeituraInventario(
                                        onVoltar = { navController.popBackStack() },
                                        usuario = usuarioAutenticado
                                    )
                                }

                                composable(Screen.Historico.route) {
                                    TelaHistorico(
                                        onVoltar = { navController.popBackStack() },
                                        onSessaoClick = { sessaoId ->
                                            navController.navigate(Screen.DetalheHistorico.createRoute(sessaoId))
                                        }
                                    )
                                }

                                composable(route = Screen.DetalheHistorico.route, arguments = Screen.DetalheHistorico.arguments) {
                                    TelaDetalheHistorico(onVoltar = { navController.popBackStack() })
                                }
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