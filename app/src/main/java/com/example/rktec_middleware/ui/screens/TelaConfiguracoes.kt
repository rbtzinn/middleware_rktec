@file:OptIn(ExperimentalMaterial3Api::class)
package com.example.rktec_middleware.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.rktec_middleware.data.model.ThemeOption
import com.example.rktec_middleware.ui.components.GradientHeader
import com.example.rktec_middleware.ui.theme.Dimens
import com.example.rktec_middleware.viewmodel.AuthViewModel
import com.example.rktec_middleware.viewmodel.ConfiguracoesViewModel
import kotlin.math.roundToInt

@Composable
fun TelaConfiguracoes(
    onVoltar: () -> Unit,
    authViewModel: AuthViewModel,
    viewModel: ConfiguracoesViewModel = hiltViewModel()
) {
    val rfIdPower by viewModel.rfIdPower.collectAsState()
    val soundEnabled by viewModel.soundFeedbackEnabled.collectAsState()
    val vibrationEnabled by viewModel.vibrationFeedbackEnabled.collectAsState()
    val themeOption by viewModel.themeOption.collectAsState()
    var mostrarDialogLogout by remember { mutableStateOf(false) }

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

    Scaffold(
        topBar = {
            // CORREÇÃO: A chamada ao GradientHeader agora passa um IconButton completo
            GradientHeader(
                title = "Configurações",
                navigationIcon = {
                    IconButton(onClick = onVoltar) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Voltar")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(Dimens.PaddingMedium),
            verticalArrangement = Arrangement.spacedBy(Dimens.PaddingLarge)
        ) {
            Text("Leitor RFID", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            SettingSliderItem(
                title = "Potência da Antena",
                value = rfIdPower.toFloat(),
                onValueChange = { viewModel.updateRfIdPower(it.roundToInt()) },
                valueRange = 1f..30f,
                steps = 28,
                icon = Icons.Default.GraphicEq
            )
            Divider()
            Text("Feedback", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            SettingSwitchItem(
                title = "Som de leitura",
                checked = soundEnabled,
                onCheckedChange = { viewModel.updateSoundFeedback(it) },
                icon = Icons.Default.NotificationsActive
            )
            SettingSwitchItem(
                title = "Vibração ao ler",
                checked = vibrationEnabled,
                onCheckedChange = { viewModel.updateVibrationFeedback(it) },
                icon = Icons.Default.Vibration
            )
            Divider()
            Text("Aparência", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            SegmentedButtonTheme(
                selectedOption = themeOption,
                onOptionSelected = { viewModel.updateThemeOption(it) }
            )
            Spacer(modifier = Modifier.weight(1f))
            OutlinedButton(
                onClick = { mostrarDialogLogout = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = Dimens.PaddingMedium),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f))
            ) {
                Icon(Icons.Default.ExitToApp, contentDescription = null)
                Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                Text("Sair da Conta")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SegmentedButtonTheme(
    selectedOption: ThemeOption,
    onOptionSelected: (ThemeOption) -> Unit
) {
    val options = listOf(
        "Claro" to ThemeOption.LIGHT,
        "Escuro" to ThemeOption.DARK,
        "Sistema" to ThemeOption.SYSTEM
    )

    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
        options.forEachIndexed { index, (label, option) ->
            val shape = when (index) {
                0 -> RoundedCornerShape(topStartPercent = 100, bottomStartPercent = 100)
                options.lastIndex -> RoundedCornerShape(topEndPercent = 100, bottomEndPercent = 100)
                else -> RoundedCornerShape(0.dp)
            }

            SegmentedButton(
                selected = selectedOption == option,
                onClick = { onOptionSelected(option) },
                shape = shape
            ) {
                Text(label)
            }
        }
    }
}

@Composable
private fun SettingSliderItem(
    title: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>,
    steps: Int,
    icon: ImageVector
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = Dimens.PaddingSmall)
    ) {
        Icon(icon, contentDescription = title, modifier = Modifier.size(Dimens.IconSizeMedium))
        Spacer(Modifier.width(Dimens.PaddingMedium))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            Text("Valor: ${value.roundToInt()}", style = MaterialTheme.typography.bodySmall)
            Slider(
                value = value,
                onValueChange = onValueChange,
                valueRange = valueRange,
                steps = steps
            )
        }
    }
}

@Composable
private fun SettingSwitchItem(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    icon: ImageVector
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = Dimens.PaddingSmall)
    ) {
        Icon(icon, contentDescription = title, modifier = Modifier.size(Dimens.IconSizeMedium))
        Spacer(Modifier.width(Dimens.PaddingMedium))
        Text(title, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}