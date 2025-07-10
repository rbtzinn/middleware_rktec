package com.example.rktec_middleware.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun DialogPromoverAdminCustom(
    onConfirmar: (String) -> Unit,
    onCancelar: () -> Unit,
    loading: Boolean,
    erroCodigo: Boolean
) {
    val AzulRktec = Color(0xFF174D86)
    val AzulClaroRktec = Color(0xFF4A90E2)
    var codigo by remember { mutableStateOf("") }

    Box(
        Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.30f))
            .wrapContentSize(Alignment.Center)
    ) {
        Card(
            shape = RoundedCornerShape(18.dp),
            modifier = Modifier
                .padding(horizontal = 36.dp)
                .shadow(10.dp, RoundedCornerShape(18.dp)),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier
                    .padding(vertical = 26.dp, horizontal = 24.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    Icons.Filled.Lock,
                    contentDescription = "Ativar Admin",
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(AzulClaroRktec.copy(alpha = 0.18f), CircleShape)
                        .padding(10.dp),
                    tint = AzulRktec
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    "Ativar modo administrador",
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                    fontSize = 19.sp,
                    color = AzulRktec
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "Digite o código secreto para ativar o modo admin.",
                    color = Color(0xFF5A6D80),
                    fontSize = 14.sp,
                    modifier = Modifier.padding(horizontal = 8.dp),
                    lineHeight = 17.sp
                )
                Spacer(Modifier.height(16.dp))
                OutlinedTextField(
                    value = codigo,
                    onValueChange = { codigo = it },
                    label = { Text("Código Secreto") },
                    isError = erroCodigo,
                    enabled = !loading,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                if (erroCodigo) {
                    Text(
                        "Código incorreto",
                        color = Color(0xFFD32F2F),
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
                Spacer(Modifier.height(18.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = { if (!loading) onCancelar() },
                        enabled = !loading
                    ) {
                        Text("Cancelar", color = AzulRktec, fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold)
                    }
                    Spacer(Modifier.width(6.dp))
                    Button(
                        onClick = { if (!loading) onConfirmar(codigo) },
                        enabled = codigo.isNotBlank() && !loading,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AzulRktec)
                    ) {
                        if (loading) {
                            CircularProgressIndicator(
                                color = Color.White,
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(18.dp)
                            )
                        } else {
                            Text("Ativar", color = Color.White, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
