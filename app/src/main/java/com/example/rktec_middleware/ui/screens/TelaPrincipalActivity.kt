package com.example.rktec_middleware.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.rktec_middleware.data.model.ExportProgress
import com.example.rktec_middleware.data.model.TipoUsuario
import com.example.rktec_middleware.ui.components.AvatarComGestoSecreto
import com.example.rktec_middleware.ui.components.StandardTextField
import com.example.rktec_middleware.ui.theme.*
import com.example.rktec_middleware.util.LogHelper
import com.example.rktec_middleware.viewmodel.AuthViewModel
import com.example.rktec_middleware.viewmodel.AuthState
import com.example.rktec_middleware.viewmodel.DashboardData
import com.example.rktec_middleware.viewmodel.TelaPrincipalViewModel
import com.example.rktec_middleware.ui.components.ConnectivityStatusIndicator
import com.example.rktec_middleware.util.ConnectivityObserver
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TelaPrincipal(
    authViewModel: AuthViewModel,
    onMenuClick: () -> Unit,
    onColetaAvulsaClick: () -> Unit,
    onInventarioClick: () -> Unit,
    onChecagemClick: () -> Unit,
    onSobreClick: () -> Unit,
    onGerenciarUsuariosClick: () -> Unit,
    onHistoricoClick: () -> Unit,
    onAjudaClick: () -> Unit,
    telaPrincipalViewModel: TelaPrincipalViewModel = hiltViewModel()
) {
    val authState by authViewModel.authState.collectAsState()
    val usuario = (authState as? AuthState.Autenticado)?.usuario
    val exportState by telaPrincipalViewModel.exportState.collectAsState()
    val dashboardData by telaPrincipalViewModel.dashboardData.collectAsState()
    val connectivityStatus by telaPrincipalViewModel.connectivityStatus.collectAsState()

    var mostrarDialogAdmin by remember { mutableStateOf(false) }
    var mostrarDialogExportar by remember { mutableStateOf(false) }
    var mostrarDialogLogout by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    LaunchedEffect(exportState) {
        when (val state = exportState) {
            is ExportProgress.Success -> {
                Toast.makeText(context, "Planilha salva em ${state.file.parent}", Toast.LENGTH_LONG).show()
                telaPrincipalViewModel.resetExportState()
            }
            is ExportProgress.Error -> {
                Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
                telaPrincipalViewModel.resetExportState()
            }
            else -> {}
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
                ) {
                    Text("Sair")
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogLogout = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    if (usuario == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val screenWidthPx = with(LocalDensity.current) { constraints.maxWidth.toFloat() }
            val offsetX = remember { Animatable(0f) }

            val closeDebugScreen: () -> Unit = {
                scope.launch {
                    offsetX.animateTo(0f, tween(300))
                }
            }

            TelaDebug(
                usuarioLogado = usuario.nome,
                onVoltar = closeDebugScreen,
                onBancoLimpo = {
                    authViewModel.setEmpresaConfigurada(false)
                }
            )

            val dragModifier = if (usuario.tipo == TipoUsuario.ADMIN) {
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
                Modifier.clickable(enabled = true) {}
            }

            Box(
                modifier = Modifier
                    .offset { IntOffset(offsetX.value.roundToInt(), 0) }
                    .then(dragModifier)
                    .fillMaxSize()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background)
                ) {
                    // ALTERAÇÃO INICIA AQUI:
                    // Este Box foi adicionado com weight(1f) para ocupar todo o espaço disponível,
                    // exceto o espaço do rodapé. O FAB será alinhado em relação a este Box,
                    // garantindo que ele não sobreponha o rodapé.
                    Box(modifier = Modifier.weight(1f)) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize() // Preenche o espaço dado pelo Box pai
                                .verticalScroll(rememberScrollState())
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(140.dp)
                                    .background(
                                        Brush.verticalGradient(
                                            0f to MaterialTheme.colorScheme.primaryContainer,
                                            1f to MaterialTheme.colorScheme.primary
                                        )
                                    )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(horizontal = Dimens.PaddingLarge, vertical = Dimens.PaddingMedium),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    AvatarComGestoSecreto(
                                        nomeUsuario = usuario.nome,
                                        onGestoDetectado = { if (usuario.tipo != TipoUsuario.ADMIN) mostrarDialogAdmin = true }
                                    )
                                    Spacer(modifier = Modifier.width(Dimens.PaddingSmall))
                                    Column {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            SimpleConnectivityIndicator(status = connectivityStatus)
                                            Spacer(modifier = Modifier.width(Dimens.PaddingSmall))
                                            Text(
                                                dashboardData.nomeEmpresa,
                                                style = MaterialTheme.typography.bodyLarge.copy(color = Color.White.copy(alpha = 0.9f))
                                            )
                                        }
                                        Text(
                                            "Bem-vindo, ${usuario.nome.split(" ").first()}",
                                            style = MaterialTheme.typography.headlineMedium.copy(color = Color.White),
                                            maxLines = 1,
                                        )
                                    }
                                    Spacer(modifier = Modifier.weight(1f))
                                    IconButton(
                                        onClick = { onMenuClick() },
                                        modifier = Modifier.background(Color.White.copy(alpha = 0.20f), CircleShape)
                                    ) {
                                        Icon(Icons.Default.Menu, contentDescription = "Menu", tint = Color.White)
                                    }
                                }
                            }

                            Column(
                                Modifier
                                    .fillMaxWidth()
                                    .offset(y = (-30).dp)
                                    .padding(horizontal = Dimens.PaddingLarge),
                                verticalArrangement = Arrangement.spacedBy(Dimens.PaddingMedium)
                            ) {
                                DashboardSection(dashboardData)
                                FeatureCard(title = "Inventário", subtitle = "Controle e acompanhe o estoque", icon = Icons.Default.ListAlt, color = RktGreen, onClick = onInventarioClick, description = "Inicia uma sessão de contagem. Compare as etiquetas lidas com a lista de itens esperados para uma loja ou setor específico, identificando sobras e faltas.")
                                FeatureCard(title = "Checagem de Item", subtitle = "Verifique uma única etiqueta", icon = Icons.Default.QrCodeScanner, color = RktBlueInfo, onClick = onChecagemClick, description = "Verifique um único item. Digite o código da etiqueta para consultar seus detalhes na base de dados e use o modo 'Localizador' para encontrá-lo fisicamente.")
                                FeatureCard(title = "Coleta Avulsa", subtitle = "Leia tags sem um inventário prévio", icon = Icons.Default.DocumentScanner, color = RktOrange, onClick = onColetaAvulsaClick, description = "Realiza uma leitura livre, sem vínculo com a base de dados. Ideal para coletar rapidamente todas as etiquetas presentes em uma área ou caixa.")
                                FeatureCard(title = "Histórico de Inventários", subtitle = "Consulte relatórios de contagens passadas", icon = Icons.Default.History, color = MaterialTheme.colorScheme.secondary, onClick = onHistoricoClick, description = "Acesse um registro detalhado de todas as sessões de inventário já realizadas, incluindo itens encontrados, faltantes e adicionais.")
                                FeatureCard(title = "Exportar Planilha Final", subtitle = "Gera o relatório mestre com os dados", icon = Icons.Default.UploadFile, color = RktBlueInfo, onClick = { mostrarDialogExportar = true }, description = "Gera e salva um arquivo de planilha (.xlsx) no dispositivo contendo o inventário completo, com todos os itens da base de dados.")
                            }
                        } // Fim do Column com scroll

                        // O FAB foi movido para dentro deste Box para que seu alinhamento
                        // respeite os limites do Box.
                        if (usuario.tipo == TipoUsuario.ADMIN) {
                            FloatingActionButton(
                                onClick = onGerenciarUsuariosClick,
                                modifier = Modifier
                                    .align(Alignment.BottomEnd) // Alinhado dentro do Box com weight(1f)
                                    .padding(end = Dimens.PaddingLarge, bottom = 4.dp),
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                            ) {
                                Icon(Icons.Default.ManageAccounts, "Gerenciar Usuários")
                            }
                        }
                    } // Fim do Box com weight(1f)
                    // ALTERAÇÃO TERMINA AQUI

                    // O Rodapé agora está fora do Box com weight, agindo como um limite inferior.
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(Dimens.PaddingMedium),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Divider(Modifier.padding(vertical = Dimens.PaddingSmall), color = RktStroke.copy(alpha = 0.5f))

                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            TextButton(onClick = onSobreClick) {
                                Icon(imageVector = Icons.Default.Info, contentDescription = "Sobre", modifier = Modifier.size(Dimens.IconSizeSmall))
                                Spacer(Modifier.width(Dimens.PaddingSmall))
                                Text("Sobre o Sistema", fontWeight = FontWeight.SemiBold)
                            }
                            Text("|", color = MaterialTheme.colorScheme.primary)
                            TextButton(onClick = onAjudaClick) {
                                Icon(imageVector = Icons.Default.HelpOutline, contentDescription = "Ajuda", modifier = Modifier.size(Dimens.IconSizeSmall))
                                Spacer(Modifier.width(Dimens.PaddingSmall))
                                Text("Ajuda e Suporte", fontWeight = FontWeight.SemiBold)
                            }
                        }

                        Text("RKTECNOLOGIAS", fontWeight = FontWeight.Bold, color = RktGreen, style = MaterialTheme.typography.bodyLarge)
                    }
                }

                // O FAB foi REMOVIDO daqui.

                if (mostrarDialogExportar) {
                    AlertDialog(
                        onDismissRequest = { mostrarDialogExportar = false },
                        icon = { Icon(Icons.Default.UploadFile, contentDescription = null) },
                        title = { Text("Confirmar Exportação") },
                        text = { Text("Deseja realmente gerar a planilha final com todos os dados do inventário?") },
                        confirmButton = {
                            Button(onClick = {
                                telaPrincipalViewModel.exportarPlanilhaCompleta()
                                mostrarDialogExportar = false
                            }) { Text("Exportar") }
                        },
                        dismissButton = { TextButton(onClick = { mostrarDialogExportar = false }) { Text("Cancelar") } }
                    )
                }

                if (mostrarDialogAdmin) {
                    DialogPromoverAdmin(
                        onConfirmar = { codigoDigitado ->
                            if (codigoDigitado == "@DM2025") {
                                authViewModel.promoverUsuarioAtualParaAdmin()
                                scope.launch {
                                    LogHelper.registrarGerenciamentoUsuario(
                                        context = context,
                                        companyId = usuario.companyId,
                                        usuarioResponsavel = "SISTEMA",
                                        acao = "PROMOÇÃO ADM",
                                        usuarioAlvo = usuario.email,
                                        motivo = "Promoção via código secreto",
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

                if (exportState is ExportProgress.InProgress) {
                    val progresso = (exportState as ExportProgress.InProgress).percent
                    ProgressOverlay(progress = progresso / 100f, text = "Exportando planilha... $progresso%")
                }
            }
        }
    }
}

@Composable
fun SimpleConnectivityIndicator(status: ConnectivityObserver.Status) {
    val color = when (status) {
        ConnectivityObserver.Status.Available -> RktGreen
        ConnectivityObserver.Status.Losing -> Color.Yellow
        else -> Color.Gray
    }
    Box(
        modifier = Modifier
            .size(8.dp)
            .clip(CircleShape)
            .background(color)
    )
}

@Composable
private fun DashboardSection(data: DashboardData) {
    Column(verticalArrangement = Arrangement.spacedBy(Dimens.PaddingSmall)) {
        data.ultimaSessao?.let { sessao ->
            val formatadorData = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }
            val acuracidade = if (sessao.totalEsperado > 0) {
                (sessao.totalEncontrado.toFloat() / sessao.totalEsperado.toFloat()) * 100
            } else 100f

            Card(
                shape = MaterialTheme.shapes.large,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(Modifier.padding(Dimens.PaddingMedium)) {
                    Text("Último Inventário", style = MaterialTheme.typography.titleMedium)
                    Text(
                        "Realizado em ${formatadorData.format(Date(sessao.dataHora))} por ${sessao.usuarioResponsavel}",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(Modifier.height(Dimens.PaddingMedium))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                        DashboardInfoItem(value = "${acuracidade.toInt()}%", label = "Acuracidade", color = RktGreen)
                        DashboardInfoItem(value = sessao.totalFaltante.toString(), label = "Faltantes", color = RktRed)
                        DashboardInfoItem(value = sessao.totalAdicional.toString(), label = "Adicionais", color = RktOrange)
                    }
                }
            }
        }

        Card(
            shape = MaterialTheme.shapes.large,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(Dimens.PaddingMedium),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Inventory2, contentDescription = "Inventário", modifier = Modifier.size(Dimens.IconSizeLarge), tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(Dimens.PaddingMedium))
                Text("Total de Ativos na Base:", style = MaterialTheme.typography.bodyLarge)
                Spacer(Modifier.weight(1f))
                Text(data.totalItensBase.toString(), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun DashboardInfoItem(value: String, label: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = color)
        Text(label, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun ProgressOverlay(
    progress: Float,
    text: String,
    modifier: Modifier = Modifier
) {
    val animatedProgress by animateFloatAsState(targetValue = progress, label = "progressAnimation")

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f))
            .clickable(enabled = false, onClick = {}),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Dimens.PaddingMedium)
        ) {
            CircularProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier.size(64.dp),
                color = Color.White,
                strokeWidth = 6.dp,
                trackColor = Color.White.copy(alpha = 0.3f)
            )
            Text(
                text = text,
                color = Color.White,
                style = MaterialTheme.typography.bodyLarge
            )
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
