package com.example.rktec_middleware.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.rktec_middleware.data.model.Usuario
import com.example.rktec_middleware.ui.theme.Dimens
import com.example.rktec_middleware.data.model.TipoUsuario

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppDrawerContent(
    usuario: Usuario,
    onNavigateToProfile: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onLogoutClick: () -> Unit,
    onNavigateToLogAtividades: () -> Unit,
    onCloseDrawer: () -> Unit // Esta função já era passada, agora vamos usá-la
) {
    ModalDrawerSheet(
        modifier = Modifier.fillMaxHeight(),
        drawerContainerColor = MaterialTheme.colorScheme.surfaceContainer,
    ) {
        // NOVO: Header com o título do app e um botão para fechar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Dimens.PaddingMedium, vertical = Dimens.PaddingSmall),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                "Menu",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary
            )
            // O botão de fechar chama a função onCloseDrawer
            IconButton(onClick = onCloseDrawer) {
                Icon(Icons.Default.Close, contentDescription = "Fechar Menu")
            }
        }
        Divider()

        // Seção de informações do usuário com melhor espaçamento
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.PaddingLarge),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            UserAvatar(name = usuario.nome, modifier = Modifier.size(80.dp))
            Spacer(Modifier.height(Dimens.PaddingMedium))
            Text(usuario.nome, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(usuario.email, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Divider(modifier = Modifier.padding(horizontal = Dimens.PaddingLarge))

        // Itens de Navegação
        Column(modifier = Modifier.padding(Dimens.PaddingMedium)) {
            NavigationDrawerItem(
                icon = { Icon(Icons.Default.AccountCircle, contentDescription = null) },
                label = { Text("Minha Conta") },
                selected = false,
                onClick = {
                    onNavigateToProfile()
                    onCloseDrawer() // Fecha o menu após o clique
                },
                shape = MaterialTheme.shapes.medium
            )
            Spacer(Modifier.height(Dimens.PaddingSmall))
            NavigationDrawerItem(
                icon = { Icon(Icons.Default.Settings, contentDescription = null) },
                label = { Text("Configurações") },
                selected = false,
                onClick = {
                    onNavigateToSettings()
                    onCloseDrawer() // Fecha o menu após o clique
                },
                shape = MaterialTheme.shapes.medium
            )
            if (usuario.tipo == TipoUsuario.ADMIN) {
                Spacer(Modifier.height(Dimens.PaddingSmall))
                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.ListAlt, contentDescription = null) },
                    label = { Text("Log de Atividades") },
                    selected = false,
                    onClick = {
                        onNavigateToLogAtividades()
                        onCloseDrawer()
                    },
                    shape = MaterialTheme.shapes.medium
                )
            }
        }


        Spacer(modifier = Modifier.weight(1f)) // Empurra o botão "Sair" para baixo

        // Divisor para separar a ação de logout
        Divider(modifier = Modifier.padding(horizontal = Dimens.PaddingLarge))

        // Botão de Sair
        NavigationDrawerItem(
            icon = { Icon(Icons.Default.ExitToApp, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
            label = { Text("Sair", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error) },
            selected = false,
            onClick = {
                onLogoutClick()
                onCloseDrawer() // Fecha o menu após o clique
            },
            modifier = Modifier.padding(Dimens.PaddingMedium),
            shape = MaterialTheme.shapes.medium
        )
    }
}