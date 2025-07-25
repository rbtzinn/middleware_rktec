package com.example.rktec_middleware.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.example.rktec_middleware.data.model.Usuario
import com.example.rktec_middleware.ui.theme.Dimens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppDrawerContent(
    usuario: Usuario,
    onNavigateToProfile: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onLogoutClick: () -> Unit,
    onCloseDrawer: () -> Unit
) {
    ModalDrawerSheet {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.PaddingMedium),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            UserAvatar(name = usuario.nome, modifier = Modifier.size(Dimens.PaddingExtraLarge * 2))
            Spacer(Modifier.height(Dimens.PaddingSmall))
            Text(usuario.nome, style = MaterialTheme.typography.titleLarge)
            Text(usuario.email, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Divider(modifier = Modifier.padding(vertical = Dimens.PaddingMedium))

        NavigationDrawerItem(
            icon = { Icon(Icons.Default.AccountCircle, contentDescription = null) },
            label = { Text("Minha Conta") },
            selected = false,
            onClick = {
                onNavigateToProfile()
                onCloseDrawer()
            },
            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
        )
        NavigationDrawerItem(
            icon = { Icon(Icons.Default.Settings, contentDescription = null) },
            label = { Text("Configurações") },
            selected = false,
            onClick = {
                onNavigateToSettings()
                onCloseDrawer()
            },
            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
        )

        Spacer(modifier = Modifier.weight(1f))

        NavigationDrawerItem(
            icon = { Icon(Icons.Default.ExitToApp, contentDescription = null) },
            label = { Text("Sair", fontWeight = FontWeight.Bold) },
            selected = false,
            onClick = {
                onLogoutClick()
                onCloseDrawer()
            },
            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
        )
    }
}