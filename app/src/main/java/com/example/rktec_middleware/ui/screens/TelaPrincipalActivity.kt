// ui/screens/TelaPrincipalActivity.kt
package com.example.rktec_middleware.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.rktec_middleware.data.db.AppDatabase
import com.example.rktec_middleware.data.dao.UsuarioDao
import com.example.rktec_middleware.data.model.TipoUsuario
import com.example.rktec_middleware.data.model.Usuario
import com.example.rktec_middleware.repository.UsuarioRepository
import com.example.rktec_middleware.ui.components.AvatarComGestoSecreto
import com.example.rktec_middleware.ui.components.StandardTextField
import com.example.rktec_middleware.ui.theme.*
import com.example.rktec_middleware.util.LogHelper
import com.example.rktec_middleware.viewmodel.AuthViewModel
import com.example.rktec_middleware.viewmodel.AuthState
import kotlinx.coroutines.launch

@Composable
fun TelaPrincipal(
    usuarioDao: UsuarioDao,
    authViewModel: AuthViewModel,
    onColetaAvulsaClick: () -> Unit,
    usuarioRepository: UsuarioRepository,
    onInventarioClick: () -> Unit,
    onChecagemClick: () -> Unit,
    onSobreClick: () -> Unit,
    onSairClick: () -> Unit,
    onGerenciarUsuariosClick: () -> Unit
) {
    val authState by authViewModel.authState.collectAsState()
    var usuario: Usuario? = null
    if (authState is AuthState.Autenticado) {
        usuario = (authState as AuthState.Autenticado).usuario
    }

    var mostrarDialogSair by remember { mutableStateOf(false) }
    var mostrarDialogAdmin by remember { mutableStateOf(false) }
    var mostrarDialogExportar by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val appDatabase = AppDatabase.getInstance(context)

    RKTecMiddlewareTheme {
        if (usuario == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            // O Box que continha o gesto foi movido para a MainActivity
            Box(modifier = Modifier.fillMaxSize()) {
                // A Column principal agora engloba TODO o conteúdo (inclusive o footer)
                // e é a responsável pela rolagem.
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background)
                        // O MODIFICADOR POINTERINPUT FOI REMOVIDO DAQUI
                        .verticalScroll(rememberScrollState())
                ) {
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
                                Text(
                                    usuario.nome.split(" ").first(), // Pega só o primeiro nome
                                    style = MaterialTheme.typography.headlineMedium.copy(color = Color.White),
                                    maxLines = 1,
                                )
                            }
                            Spacer(modifier = Modifier.weight(1f)) // Empurra o botão de sair para o final
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
                        Modifier
                            .fillMaxWidth()
                            .offset(y = (-30).dp)
                            .padding(horizontal = Dimens.PaddingLarge),
                        verticalArrangement = Arrangement.spacedBy(Dimens.PaddingMedium)
                    ) {
                        FeatureCard(
                            title = "Inventário",
                            subtitle = "Controle e acompanhe o estoque",
                            icon = Icons.Default.ListAlt,
                            color = RktGreen,
                            onClick = onInventarioClick,
                            description = "Inicia uma sessão de contagem. Compare as etiquetas lidas com a lista de itens esperados para uma loja ou setor específico, identificando sobras e faltas."
                        )

                        FeatureCard(
                            title = "Checagem de Item",
                            subtitle = "Verifique uma única etiqueta",
                            icon = Icons.Default.QrCodeScanner,
                            color = RktBlueInfo,
                            onClick = onChecagemClick,
                            description = "Verifique um único item. Digite o código da etiqueta para consultar seus detalhes na base de dados e use o modo 'Localizador' para encontrá-lo fisicamente."
                        )

                        FeatureCard(
                            title = "Coleta Avulsa",
                            subtitle = "Leia tags sem um inventário prévio",
                            icon = Icons.Default.DocumentScanner,
                            color = RktOrange,
                            onClick = onColetaAvulsaClick,
                            description = "Realiza uma leitura livre, sem vínculo com a base de dados. Ideal para coletar rapidamente todas as etiquetas presentes em uma área ou caixa."
                        )

                        FeatureCard(
                            title = "Exportar Planilha Final",
                            subtitle = "Gera o relatório mestre com os dados",
                            icon = Icons.Default.UploadFile,
                            color = RktBlueInfo,
                            onClick = { mostrarDialogExportar = true },
                            description = "Gera e salva um arquivo de planilha (.xlsx) no dispositivo contendo o inventário completo, com todos os itens da base de dados."
                        )
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    // Footer (Agora dentro da coluna rolável)
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(Dimens.PaddingMedium),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
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
                if (mostrarDialogExportar) {
                    AlertDialog(
                        onDismissRequest = { mostrarDialogExportar = false },
                        icon = { Icon(Icons.Default.UploadFile, contentDescription = null) },
                        title = { Text("Confirmar Exportação") },
                        text = { Text("Deseja realmente gerar a planilha final com todos os dados do inventário?") },
                        confirmButton = {
                            Button(onClick = {
                                scope.launch {
                                    Toast.makeText(context, "Gerando planilha final...", Toast.LENGTH_SHORT).show()
                                    val arquivo = LogHelper.exportarPlanilhaCompleta(context, appDatabase)
                                    Toast.makeText(
                                        context,
                                        if (arquivo != null) "Planilha salva em ${arquivo.parent}" else "Falha ao gerar a planilha.",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                                mostrarDialogExportar = false
                            }) {
                                Text("Exportar")
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { mostrarDialogExportar = false }) {
                                Text("Cancelar")
                            }
                        }
                    )
                }

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
    title: String,
    subtitle: String,
    icon: ImageVector,
    color: Color,
    description: String,
    onClick: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val rotationAngle by animateFloatAsState(targetValue = if (expanded) 180f else 0f, label = "rotation")

    Card(
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clickable(onClick = onClick)
                    .padding(Dimens.PaddingMedium)
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
                IconButton(onClick = { expanded = !expanded }) {
                    Icon(
                        Icons.Default.ExpandMore,
                        contentDescription = "Expandir",
                        tint = RktTextSecondary,
                        modifier = Modifier.rotate(rotationAngle)
                    )
                }
            }

            AnimatedVisibility(
                visible = expanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column {
                    Divider(modifier = Modifier.padding(horizontal = Dimens.PaddingMedium), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = RktTextSecondary,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(Dimens.PaddingMedium)
                    )
                }
            }
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
