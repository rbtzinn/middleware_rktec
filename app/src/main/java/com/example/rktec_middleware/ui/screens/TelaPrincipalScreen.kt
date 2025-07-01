package com.example.rktec_middleware.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.rktec_middleware.viewmodel.RfidViewModel

@Composable
fun TelaPrincipalScreen(viewModel: RfidViewModel) {
    val tags by viewModel.tagList.collectAsState()
    var lendo by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row {
            Button(
                onClick = {
                    viewModel.startReading()
                    lendo = true
                },
                enabled = !lendo
            ) { Text("Iniciar Leitura") }

            Spacer(Modifier.width(8.dp))

            Button(
                onClick = {
                    viewModel.stopReading()
                    lendo = false
                },
                enabled = lendo
            ) { Text("Parar") }
        }

        Spacer(Modifier.height(16.dp))

        Text(
            text = if (lendo) "Lendo etiquetas..." else "Leitura parada.",
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(Modifier.height(16.dp))

        LazyColumn(modifier = Modifier.weight(1f)) {
            items(tags) { tag ->
                Text(text = tag.epc, modifier = Modifier.padding(8.dp))
            }
        }
    }
}


