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
    Column {
        Row {
            Button(onClick = { viewModel.startReading() }) {
                Text("Iniciar Leitura")
            }
            Spacer(Modifier.width(8.dp))
            Button(onClick = { viewModel.stopReading() }) {
                Text("Parar")
            }
        }
        LazyColumn {
            items(tags) { tag ->
                Text(text = tag.epc, modifier = Modifier.padding(8.dp))
            }
        }
    }
}

