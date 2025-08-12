package com.example.rktec_middleware.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.rktec_middleware.data.model.TipoUsuario
import com.example.rktec_middleware.ui.components.AvatarComGestoSecreto
import com.example.rktec_middleware.ui.theme.Dimens
import com.example.rktec_middleware.viewmodel.AuthViewModel
import com.example.rktec_middleware.viewmodel.AuthState
import com.example.rktec_middleware.viewmodel.PerfilViewModel
import androidx.compose.ui.draw.shadow


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TelaPerfil(
    onVoltar: () -> Unit,
    authViewModel: AuthViewModel,
    perfilViewModel: PerfilViewModel = hiltViewModel()
) {
    val scrollState = rememberScrollState()
    val authState by authViewModel.authState.collectAsState()
    val usuario = (authState as? AuthState.Autenticado)?.usuario
    val uiState by perfilViewModel.uiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(uiState.toastMessage) {
        uiState.toastMessage?.let { message ->
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
            perfilViewModel.onToastShown()
        }
    }

    if (uiState.showLogoutDialog) {
        AlertDialog(
            onDismissRequest = perfilViewModel::onDismissLogoutDialog,
            icon = { Icon(Icons.Default.ExitToApp, contentDescription = null) },
            title = { Text("Confirmar Saída") },
            text = { Text("Tem certeza que deseja sair da sua conta?") },
            confirmButton = {
                Button(
                    onClick = perfilViewModel::onConfirmLogout,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Sair")
                }
            },
            dismissButton = {
                TextButton(onClick = perfilViewModel::onDismissLogoutDialog) {
                    Text("Cancelar")
                }
            }
        )
    }

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        if (usuario != null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
            ) {

                ProfileHeaderCompleto(
                    nome = usuario.nome,
                    email = usuario.email,
                    onClose = onVoltar
                )

                Spacer(modifier = Modifier.height(Dimens.PaddingSmall))

                Column(
                    modifier = Modifier.padding(horizontal = Dimens.PaddingLarge),
                    verticalArrangement = Arrangement.spacedBy(Dimens.PaddingMedium)
                ) {
                    ElevatedSection(
                        title = "Minhas Informações",
                        content = {
                            InfoCard(
                                icon = Icons.Default.Person,
                                label = "Nome Completo",
                                value = usuario.nome
                            )
                            InfoCard(
                                icon = Icons.Default.Email,
                                label = "E-mail",
                                value = usuario.email
                            )
                            InfoCard(
                                icon = Icons.Default.AdminPanelSettings,
                                label = "Tipo de Conta",
                                value = if (usuario.tipo == TipoUsuario.ADMIN) "Administrador" else "Membro"
                            )
                        }
                    )

                    Spacer(modifier = Modifier.height(Dimens.PaddingSmall))

                    ElevatedSection(
                        title = "Ações da Conta",
                        content = {
                            ActionCard(
                                icon = Icons.Default.Lock,
                                text = "Alterar Senha",
                                isLoading = uiState.isLoadingPasswordReset,
                                onClick = perfilViewModel::onSendPasswordResetEmail
                            )
                        }
                    )

                    Spacer(modifier = Modifier.height(Dimens.PaddingLarge))

                    OutlinedButton(
                        onClick = perfilViewModel::onLogoutClicked,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f))
                    ) {
                        Icon(Icons.Default.ExitToApp, contentDescription = "Sair")
                        Spacer(Modifier.width(Dimens.PaddingSmall))
                        Text("Sair da Conta", fontWeight = FontWeight.SemiBold)
                    }

                    Spacer(modifier = Modifier.height(Dimens.PaddingMedium))
                }
            }
        } else {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
    }
}


@Composable
private fun ProfileHeaderCompleto(
    nome: String,
    email: String,
    onClose: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary,
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                    )
                )
            )
            .padding(top = 40.dp, bottom = 32.dp), // espaço para status bar
    ) {
        // Botão de fechar no canto
        IconButton(
            onClick = onClose,
            modifier = Modifier.align(Alignment.TopStart)
        ) {
            Icon(
                Icons.Default.Close,
                contentDescription = "Fechar",
                tint = Color.White
            )
        }

        // Conteúdo central
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.align(Alignment.Center)
        ) {
            Surface(
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f),
                shape = CircleShape,
                modifier = Modifier.size(96.dp),
                border = BorderStroke(2.dp, Color.White.copy(alpha = 0.3f))
            ) {
                Box(contentAlignment = Alignment.Center) {
                    AvatarComGestoSecreto(
                        nomeUsuario = nome,
                        onGestoDetectado = {}
                    )
                }
            }
            Text(
                text = nome,
                style = MaterialTheme.typography.headlineSmall,
                color = Color.White,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = email,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.9f)
            )
        }
    }
}

@Composable
private fun ElevatedSection(
    title: String,
    content: @Composable () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 6.dp,
                shape = MaterialTheme.shapes.large,
                spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
            ),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(vertical = Dimens.PaddingMedium)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = Dimens.PaddingLarge, vertical = 8.dp)
            )
            Divider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                thickness = 1.dp,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            content()
        }
    }
}

@Composable
private fun InfoCard(
    icon: ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Dimens.PaddingLarge, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.width(20.dp))
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Normal,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ActionCard(
    icon: ImageVector,
    text: String,
    isLoading: Boolean = false,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        enabled = !isLoading,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Dimens.PaddingLarge),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = text,
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(20.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Normal,
                modifier = Modifier.weight(1f)
            )
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.primary
                )
            } else {
                Icon(
                    Icons.Default.KeyboardArrowRight,
                    contentDescription = "Avançar",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
                }
            }
        }
    }
