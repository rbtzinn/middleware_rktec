// ui/screens/TelaPrincipal.kt
package com.example.rktec_middleware.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.rktec_middleware.data.db.AppDatabase
import com.example.rktec_middleware.data.dao.UsuarioDao
import com.example.rktec_middleware.data.model.TipoUsuario
import com.example.rktec_middleware.data.model.Usuario // Import necessário
import com.example.rktec_middleware.repository.UsuarioRepository
import com.example.rktec_middleware.ui.components.AvatarComGestoSecreto
import com.example.rktec_middleware.ui.components.StandardTextField
import com.example.rktec_middleware.ui.theme.*
import com.example.rktec_middleware.util.LogHelper
import com.example.rktec_middleware.viewmodel.AuthViewModel
import com.example.rktec_middleware.viewmodel.AuthState // Import necessário
import kotlinx.coroutines.launch

@Composable
fun TelaPrincipal(
    usuarioDao: UsuarioDao,
    authViewModel: AuthViewModel,
    onColetaAvulsaClick: () -> Unit,
    usuarioRepository: UsuarioRepository,
    onInventarioClick: () -> Unit,
    onDebugClick: () -> Unit,
    onSobreClick: () -> Unit,
    onSairClick: () -> Unit,
    onGerenciarUsuariosClick: () -> Unit
) {
    // --- CORREÇÃO 1: Acessar o usuário através do novo authState ---
    val authState by authViewModel.authState.collectAsState()
    var usuario: Usuario? = null
    if (authState is AuthState.Autenticado) {
        usuario = (authState as AuthState.Autenticado).usuario
    }

    var mostrarDialogSair by remember { mutableStateOf(false) }
    var mostrarDialogAdmin by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val appDatabase = AppDatabase.getInstance(context)

    RKTecMiddlewareTheme {
        if (usuario == null) {
            // Mostra um carregamento enquanto o estado do usuário é resolvido
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Box(modifier = Modifier.fillMaxSize()) {
                Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
                    // Header
                    Box(
                        modifier = Modifier.fillMaxWidth().height(140.dp).background(
                            Brush.verticalGradient(0f to MaterialTheme.colorScheme.primaryContainer, 1f to MaterialTheme.colorScheme.primary)
                        )
                    ) {
                        Row(
                            Modifier.fillMaxSize().padding(horizontal = Dimens.PaddingLarge, vertical = Dimens.PaddingMedium),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AvatarComGestoSecreto(
                                nomeUsuario = usuario.nome,
                                onGestoDetectado = { if (usuario.tipo != TipoUsuario.ADMIN) mostrarDialogAdmin = true }
                            )
                            Spacer(modifier = Modifier.width(Dimens.PaddingMedium))
                            Column {
                                Text("Bem-vindo!", style = MaterialTheme.typography.bodyLarge.copy(color = Color.White.copy(alpha = 0.9f)))
                                Text(usuario.nome, style = MaterialTheme.typography.headlineMedium.copy(color = Color.White))
                            }
                            Spacer(modifier = Modifier.weight(1f))
                            IconButton(
                                onClick = { mostrarDialogSair = true },
                                modifier = Modifier.background(Color.White.copy(alpha = 0.20f), CircleShape)
                            ) {
                                Icon(Icons.Default.ExitToApp, contentDescription = "Sair", tint = Color.White, modifier = Modifier.size(Dimens.IconSizeMedium))
                            }
                        }
                    }

                    // Feature Cards
                    Column(
                        Modifier.fillMaxWidth().offset(y = (-30).dp).padding(horizontal = Dimens.PaddingLarge),
                        verticalArrangement = Arrangement.spacedBy(Dimens.PaddingMedium)
                    ) {
                            FeatureCard(
                                title = "Inventário", subtitle = "Controle e acompanhe o estoque",
                                icon = Icons.Default.ListAlt, color = RktGreen, onClick = onInventarioClick
                            )

                            // --- 2. BOTÃO ADICIONADO DE VOLTA ---
                            FeatureCard(
                                title = "Coleta Avulsa", subtitle = "Leia tags sem um inventário prévio",
                                icon = Icons.Default.DocumentScanner, // Ícone sugestivo
                                color = RktOrange, // Cor sugestiva
                                onClick = onColetaAvulsaClick
                            )
                        FeatureCard(
                            title = "Exportar Planilha Final", subtitle = "Gera o relatório mestre com os dados",
                            icon = Icons.Default.UploadFile, color = RktBlueInfo,
                            onClick = {
                                scope.launch {
                                    Toast.makeText(context, "Gerando planilha final...", Toast.LENGTH_SHORT).show()
                                    val arquivo = LogHelper.exportarPlanilhaCompleta(context, appDatabase)
                                    Toast.makeText(
                                        context,
                                        if (arquivo != null) "Planilha salva em ${arquivo.parent}" else "Falha ao gerar a planilha.",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            }
                        )
                    }

                    Spacer(modifier = Modifier.weight(1f))


                    // Footer
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(Dimens.PaddingMedium),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        if (usuario.tipo == TipoUsuario.ADMIN) {
                            TextButton(onClick = onDebugClick) {
                                Text("Consulta e Edição de Itens", color = RktTextSecondary)
                            }
                        }
                        Divider(Modifier.padding(vertical = Dimens.PaddingSmall), color = RktStroke.copy(alpha = 0.5f))
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            TextButton(onClick = onSobreClick) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = "Sobre",
                                    tint = RktGreen,
                                    modifier = Modifier.size(Dimens.IconSizeSmall)
                                )
                                Spacer(Modifier.width(Dimens.PaddingSmall))
                                Text("Sobre o Sistema", color = RktGreen, fontWeight = FontWeight.SemiBold)
                            }
                        }
                        Text(
                            "RKTECNOLOGIAS",
                            fontWeight = FontWeight.Bold,
                            color = RktGreen,
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            "Todos os direitos reservados",
                            style = MaterialTheme.typography.labelMedium
                        )

                    }
                }

                if (usuario.tipo == TipoUsuario.ADMIN) {
                    FloatingActionButton(
                        onClick = onGerenciarUsuariosClick,
                        modifier = Modifier.align(Alignment.BottomEnd).padding(Dimens.PaddingLarge),
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ) {
                        Icon(Icons.Default.ManageAccounts, "Gerenciar Usuários")
                    }
                }

                // --- DIALOGS ---
                if (mostrarDialogSair) {
                    AlertDialog(
                        onDismissRequest = { mostrarDialogSair = false },
                        title = { Text("Confirmar Saída") },
                        text = { Text("Deseja realmente sair do aplicativo?") },
                        confirmButton = { Button(onClick = onSairClick) { Text("Sair") } },
                        dismissButton = { TextButton(onClick = { mostrarDialogSair = false }) { Text("Cancelar") } }
                    )
                }

                if (mostrarDialogAdmin) {
                    DialogPromoverAdmin(
                        onConfirmar = { codigoDigitado ->
                            if (codigoDigitado == "@DM2025") {
                                scope.launch {
                                    val usuarioPromovido = usuario.copy(tipo = TipoUsuario.ADMIN)
                                    usuarioRepository.atualizarUsuario(usuarioPromovido)
                                    // --- CORREÇÃO 2: Chamar a função sem argumentos ---
                                    authViewModel.recarregarUsuario()
                                    LogHelper.registrarGerenciamentoUsuario(
                                        context = context, usuarioResponsavel = "SISTEMA", acao = "PROMOÇÃO ADM",
                                        usuarioAlvo = usuario.email, motivo = "Promoção via código secreto",
                                        detalhes = "Usuário promovido a ADMIN pelo código secreto."
                                    )
                                    Toast.makeText(context, "Permissões de Administrador concedidas!", Toast.LENGTH_SHORT).show()
                                    mostrarDialogAdmin = false
                                }
                                return@DialogPromoverAdmin true
                            } else {
                                return@DialogPromoverAdmin false
                            }
                        },
                        onCancelar = { mostrarDialogAdmin = false }
                    )
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FeatureCard(
    title: String, subtitle: String, icon: ImageVector, color: Color, onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(Dimens.PaddingMedium)
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon,
                    contentDescription = title,
                    tint = color,
                    modifier = Modifier.size(Dimens.IconSizeLarge)
                )
            }
            Spacer(modifier = Modifier.width(Dimens.PaddingMedium))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, style = MaterialTheme.typography.titleLarge)
                Text(text = subtitle, style = MaterialTheme.typography.bodyMedium)
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = RktTextSecondary)
        }
    }
}

@Composable
private fun DialogPromoverAdmin(
    onConfirmar: (String) -> Boolean,
    onCancelar: () -> Unit
) {
    var codigo by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var showError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onCancelar,
        shape = MaterialTheme.shapes.large,
        icon = { Icon(Icons.Default.AdminPanelSettings, contentDescription = null) },
        title = { Text("Acesso de Administrador") },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Digite o código de acesso para obter permissões de administrador.")
                Spacer(Modifier.height(Dimens.PaddingMedium))
                StandardTextField(
                    value = codigo,
                    onValueChange = {
                        codigo = it
                        showError = false
                    },
                    label = "Código de Acesso"
                )
                AnimatedVisibility(visible = showError) {
                    Text(
                        "Código inválido!",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = Dimens.PaddingSmall)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    isLoading = true
                    showError = !onConfirmar(codigo)
                    isLoading = false
                },
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(Dimens.IconSizeSmall), color = Color.White)
                } else {
                    Text("Confirmar")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onCancelar) { Text("Cancelar") }
        }
    )
}