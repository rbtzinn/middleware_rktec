package com.example.rktec_middleware.ui.screens

import android.Manifest
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.rktec_middleware.ui.components.PrimaryButton
import com.example.rktec_middleware.ui.theme.Dimens
import com.example.rktec_middleware.ui.theme.RktGreen
import com.example.rktec_middleware.data.model.Usuario

@Composable
fun TelaImportacao(
    onConcluido: (String) -> Unit,
    usuario: Usuario,
    onSobreClick: () -> Unit
) {
    var uriParaMapeamento by remember { mutableStateOf<Uri?>(null) }
    var erro by remember { mutableStateOf<String?>(null) }
    var permissaoConcedida by remember { mutableStateOf(true) }

    val requestPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            permissaoConcedida = granted
            if (!granted) erro = "Permissão de armazenamento negada."
        }
    )

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
    }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri: Uri? ->
            uri?.let {
                erro = null
                uriParaMapeamento = it
            } ?: run {
                erro = "Nenhum arquivo selecionado"
            }
        }
    )

    if (uriParaMapeamento != null) {
        TelaMapeamentoPlanilha(
            uri = uriParaMapeamento!!,
            usuario = usuario,
            onSalvar = { totalItens ->
                uriParaMapeamento = null
                onConcluido("Importação de $totalItens itens concluída com sucesso!")
            },
            onCancelar = {
                uriParaMapeamento = null
            }
        )
        return
    }

    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        // Header
        Box(
            modifier = Modifier.fillMaxWidth().height(120.dp).background(
                Brush.verticalGradient(
                    0f to MaterialTheme.colorScheme.primaryContainer,
                    1f to MaterialTheme.colorScheme.primary
                )
            ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "Importar Planilha",
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White,
                textAlign = TextAlign.Center
            )
        }

        // Card de Ação
        Card(
            modifier = Modifier.fillMaxWidth().offset(y = (-30).dp).padding(horizontal = Dimens.PaddingLarge),
            shape = MaterialTheme.shapes.large,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(Modifier.padding(Dimens.PaddingLarge), verticalArrangement = Arrangement.spacedBy(Dimens.PaddingMedium)) {
                Text(
                    "Importe a base de inventário",
                    style = MaterialTheme.typography.titleLarge
                )
                Text(
                    "Selecione o arquivo (.csv, .xls, .xlsx) que será usado para identificar os itens e construir o banco de dados local.",
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(Modifier.height(Dimens.PaddingSmall))
                PrimaryButton(
                    onClick = { filePickerLauncher.launch("*/*") },
                    text = "Selecionar Planilha",
                    enabled = permissaoConcedida
                )
                erro?.let {
                    Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }

        Spacer(Modifier.weight(1f))

        // Footer
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth().padding(bottom = Dimens.PaddingSmall)
        ) {
            TextButton(onClick = onSobreClick) {
                Text("RKTECNOLOGIAS", fontWeight = FontWeight.Bold, color = RktGreen)
            }
            Text(
                "Todos os direitos reservados — RKTECNOLOGIAS",
                style = MaterialTheme.typography.labelMedium
            )
        }
    }
}