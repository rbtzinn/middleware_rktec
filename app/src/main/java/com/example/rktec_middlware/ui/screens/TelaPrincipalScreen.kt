package com.example.rktec_middleware.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun TelaPrincipalScreen() {
    val leituras = remember { mutableStateListOf("EPC123", "EPC456", "EPC789") }

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)) {

        Text("Etiquetas Lidas", style = MaterialTheme.typography.headlineSmall)

        LazyColumn(modifier = Modifier.weight(1f)) {
            items(leituras) { epc ->
                Text(text = epc, modifier = Modifier.padding(8.dp))
            }
        }

        Button(
            onClick = { /* depois colocamos a importação da planilha aqui */ },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Importar Planilha")
        }
    }
}
