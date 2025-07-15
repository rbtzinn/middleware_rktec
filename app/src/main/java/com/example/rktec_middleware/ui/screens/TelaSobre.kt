// ui/screens/TelaSobre.kt
package com.example.rktec_middleware.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import com.example.rktec_middleware.R
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.rktec_middleware.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TelaSobre(onVoltar: () -> Unit) {
    RKTecMiddlewareTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Sobre o Aplicativo") },
                    navigationIcon = {
                        IconButton(onClick = onVoltar) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Voltar")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
            }
        ) { paddingValues ->
            LazyColumn(
                modifier = Modifier.padding(paddingValues).fillMaxSize().background(MaterialTheme.colorScheme.background),
                contentPadding = PaddingValues(Dimens.PaddingMedium),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(Dimens.PaddingLarge)
            ) {
                item {
                    Card(
                        shape = MaterialTheme.shapes.large,
                        elevation = CardDefaults.cardElevation(4.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(Dimens.PaddingLarge)
                        ) {
                            Surface(
                                shape = CircleShape, color = MaterialTheme.colorScheme.primary, tonalElevation = 8.dp,
                                modifier = Modifier.size(100.dp)
                            ) {
                                Icon(Icons.Default.SettingsInputAntenna, "Logo RFID", tint = Color.White, modifier = Modifier.padding(24.dp))
                            }
                            Spacer(Modifier.height(Dimens.PaddingMedium))
                            Text("RKTEC MIDDLEWARE", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black)
                            Text("Solução profissional para inventário de ativos RFID", style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Center)
                            Spacer(Modifier.height(Dimens.PaddingLarge))
                            Divider(color = RktStroke.copy(alpha = 0.5f))
                            Spacer(Modifier.height(Dimens.PaddingMedium))

                            FeatureItem(icon = Icons.Default.Storage, title = "Banco de Dados Local", description = "Armazenamento eficiente e offline com SQLite.")
                            FeatureItem(icon = Icons.Default.Security, title = "Integração com Firebase", description = "Sincronização segura e escalável com a nuvem.")
                            FeatureItem(icon = Icons.Default.IntegrationInstructions, title = "Exportação de Relatórios", description = "Geração de planilhas em Excel e logs detalhados.")

                            Spacer(Modifier.height(Dimens.PaddingMedium))
                            Text("Versão 1.0.0", style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }

                item { TeamSection() }
            }
        }
    }
}

@Composable
private fun FeatureItem(icon: ImageVector, title: String, description: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = Dimens.PaddingSmall),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Dimens.PaddingMedium)
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(Dimens.IconSizeLarge))
        Column {
            Text(title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
            Text(description, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun TeamSection() {
    Column(
        modifier = Modifier.fillMaxWidth()
            .clip(MaterialTheme.shapes.large)
            .background(RktBlueLight)
            .padding(Dimens.PaddingMedium)
    ) {
        Text(
            "EQUIPE DE DESENVOLVIMENTO", style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier.padding(bottom = Dimens.PaddingSmall)
        )
        TeamMember(name = "Roberto Gabriel", role = "Tech Lead", photoRes = R.drawable.foto_roberto, linkedinUrl = "https://www.linkedin.com/in/roberto-gabriel-ara%C3%BAjo-miranda/", githubUrl = "https://github.com/rbtzinn", instagramUrl = "https://instagram.com/rgabriel.04")
        Divider(Modifier.padding(horizontal = Dimens.PaddingMedium), color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
        TeamMember(name = "Kawã Vinicius", role = "Mobile Developer", photoRes = R.drawable.foto_kawa, linkedinUrl = "https://www.linkedin.com/in/kaw%C3%A3-vin%C3%ADcius-020438243/", githubUrl = "https://github.com/IVCOLT", instagramUrl = "https://instagram.com/vini_2oo4")
    }
}

@Composable
private fun TeamMember(
    name: String, role: String, photoRes: Int, linkedinUrl: String?, githubUrl: String?, instagramUrl: String?
) {
    var expanded by remember { mutableStateOf(false) }
    val rotation by animateFloatAsState(targetValue = if (expanded) 90f else 0f, label = "rotation")

    // A CORREÇÃO É AQUI: de "expanded.value" para apenas "expanded"
    Column(Modifier.clickable { expanded = !expanded }) {
        Row(
            modifier = Modifier.padding(Dimens.PaddingSmall),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Dimens.PaddingMedium)
        ) {
            Image(
                painter = painterResource(id = photoRes), contentDescription = "Foto de $name",
                modifier = Modifier.size(64.dp).clip(CircleShape).border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
            )
            Column(Modifier.weight(1f)) {
                Text(name, style = MaterialTheme.typography.titleLarge)
                Text(role, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
            }
            Icon(Icons.Default.ChevronRight, "Expandir", modifier = Modifier.rotate(rotation))
        }

        if (expanded) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(start = 80.dp), // Alinhado com o nome
                horizontalArrangement = Arrangement.spacedBy(Dimens.PaddingSmall)
            ) {
                SocialIcon(url = linkedinUrl, iconRes = R.drawable.ic_linkedin, "LinkedIn")
                SocialIcon(url = githubUrl, iconRes = R.drawable.ic_github, "GitHub")
                SocialIcon(url = instagramUrl, iconRes = R.drawable.ic_instagram, "Instagram")
            }
        }
    }
}

@Composable
private fun SocialIcon(url: String?, iconRes: Int, contentDescription: String) {
    val context = LocalContext.current
    url?.let {
        IconButton(onClick = {
            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(it)))
        }) {
            Icon(painterResource(id = iconRes), contentDescription, modifier = Modifier.size(Dimens.IconSizeSmall), tint = RktTextSecondary)
        }
    }
}