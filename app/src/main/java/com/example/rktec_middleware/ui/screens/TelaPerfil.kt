package com.example.rktec_middleware.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import com.example.rktec_middleware.data.model.TipoUsuario
import com.example.rktec_middleware.ui.components.GradientHeader
import com.example.rktec_middleware.ui.theme.Dimens
import com.example.rktec_middleware.viewmodel.AuthViewModel
import com.example.rktec_middleware.viewmodel.AuthState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TelaPerfil(
    onVoltar: () -> Unit,
    authViewModel: AuthViewModel
) {
    val authState by authViewModel.authState.collectAsState()
    val usuario = (authState as? AuthState.Autenticado)?.usuario

    Scaffold(
        topBar = {
            GradientHeader(title = "Minha Conta", onVoltar = onVoltar)
        }
    ) { paddingValues ->
        if (usuario != null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(Dimens.PaddingLarge),
                verticalArrangement = Arrangement.spacedBy(Dimens.PaddingMedium)
            ) {
                ProfileInfoCard(
                    icon = Icons.Default.Person,
                    label = "Nome Completo",
                    value = usuario.nome
                )
                ProfileInfoCard(
                    icon = Icons.Default.Email,
                    label = "Email",
                    value = usuario.email
                )
                ProfileInfoCard(
                    icon = Icons.Default.AdminPanelSettings,
                    label = "Tipo de Conta",
                    value = if (usuario.tipo == TipoUsuario.ADMIN) "Administrador" else "Membro"
                )
            }
        } else {
            Box(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
    }
}

@Composable
private fun ProfileInfoCard(
    icon: ImageVector,
    label: String,
    value: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier.padding(Dimens.PaddingLarge),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                modifier = Modifier.size(Dimens.IconSizeLarge),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(Dimens.PaddingMedium))
            Column {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}