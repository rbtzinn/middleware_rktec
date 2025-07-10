package com.example.rktec_middleware.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun DialogLogoutCustom(
    onConfirmar: () -> Unit,
    onCancelar: () -> Unit,
    loading: Boolean
) {
    // Paleta do app
    val AzulRktec = Color(0xFF174D86)
    val AzulClaroRktec = Color(0xFF4A90E2)

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
                    .padding(vertical = 30.dp, horizontal = 26.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    Icons.Filled.ExitToApp,
                    contentDescription = "Sair",
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(AzulClaroRktec.copy(alpha = 0.18f), CircleShape)
                        .padding(10.dp),
                    tint = AzulRktec
                )
                Spacer(Modifier.height(10.dp))
                Text(
                    "Deseja realmente sair?",
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                    fontSize = 20.sp,
                    color = AzulRktec
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "Você precisará fazer login novamente para acessar o app.",
                    color = Color(0xFF5A6D80),
                    fontSize = 14.sp,
                    modifier = Modifier.padding(horizontal = 8.dp),
                    lineHeight = 17.sp
                )
                Spacer(Modifier.height(20.dp))
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
                        onClick = { if (!loading) onConfirmar() },
                        enabled = !loading,
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
                            Text("Sair", color = Color.White, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
