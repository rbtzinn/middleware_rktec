package com.example.rktec_middleware.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.text.font.FontWeight
import com.example.rktec_middleware.ui.components.GradientHeader
import com.example.rktec_middleware.ui.theme.Dimens
import com.example.rktec_middleware.ui.theme.RktTextSecondary

// Data class para organizar as perguntas e respostas
data class FaqItem(
    val pergunta: String,
    val resposta: String
)

// Lista de Perguntas e Respostas (você pode adicionar/editar aqui)
val faqList = listOf(
    FaqItem(
        "Como funciona a importação de planilhas?",
        "Na tela de importação, selecione um arquivo (.csv, .xls, .xlsx). Em seguida, na tela de mapeamento, associe as colunas da sua planilha (como 'EPC', 'Nome do Ativo') aos campos do sistema. Após confirmar, nosso servidor processará o arquivo e criará sua base de dados."
    ),
    FaqItem(
        "O que acontece se eu importar uma nova planilha para minha empresa?",
        "Ao importar uma nova planilha, a base de dados anterior é completamente substituída. A nova planilha se torna a única 'fonte da verdade' para o seu inventário."
    ),
    FaqItem(
        "O que significa cada status na tela de Leitura de Inventário?",
        "• Verde (Encontrado): O item foi lido e pertence a este local.\n" +
                "• Amarelo (Moviddo de Setor): O item foi lido, pertence a esta loja, mas estava em outro setor. O sistema tenta corrigir o setor automaticamente.\n" +
                "• Cinza (Em Outra Loja): O item foi lido, mas pertence a outra loja da sua empresa.\n" +
                "• Vermelho (Adicional): O item lido não consta na sua base de dados original."
    ),
    FaqItem(
        "Como as edições de itens funcionam em tempo real?",
        "Quando um usuário (como um admin na Tela de Debug) edita um item, a alteração é salva no seu celular e enviada para a nuvem. Todos os outros dispositivos da mesma empresa que estiverem com o app aberto receberão essa atualização automaticamente em segundos, mantendo todos sincronizados."
    ),
    FaqItem(
        "Como um usuário demitido pode entrar em outra empresa?",
        "Se um usuário desativado tentar fazer login, ele será levado a uma tela de 'Reativação'. Nessa tela, ele poderá inserir o código de convite da nova empresa para ter seu acesso reativado e ser transferido."
    )
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TelaAjuda(
    onVoltar: () -> Unit
) {
    Scaffold(
        topBar = {
            GradientHeader(
                title = "Ajuda e Suporte",
                subtitle = "Perguntas Frequentes",
                onVoltar = onVoltar
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier.padding(paddingValues).fillMaxSize(),
            contentPadding = PaddingValues(Dimens.PaddingMedium),
            verticalArrangement = Arrangement.spacedBy(Dimens.PaddingMedium)
        ) {
            items(faqList) { faqItem ->
                FaqCard(item = faqItem)
            }
            item {
                ContatoDuvidas(
                    email = "rbtgabriel04@gmail.com",
                    whatsapp = "+55 00 00000-0000" // Se quiser colocar número real, substitua aqui
                )
            }
        }
    }
}

@Composable
fun ContatoDuvidas(email: String, whatsapp: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(Modifier.padding(Dimens.PaddingLarge)) {
            Text(
                text = "Dúvidas? Fale conosco!",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = Dimens.PaddingMedium)
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Dimens.PaddingMedium)
            ) {
                Icon(
                    imageVector = Icons.Default.Email,
                    contentDescription = "Email",
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(text = email, style = MaterialTheme.typography.bodyLarge)
            }
            Spacer(modifier = Modifier.height(Dimens.PaddingSmall))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Dimens.PaddingMedium)
            ) {
                Icon(
                    painter = rememberVectorPainter(image = androidx.compose.material.icons.Icons.Default.Phone), // ícone telefone
                    contentDescription = "WhatsApp",
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(text = whatsapp, style = MaterialTheme.typography.bodyLarge)
            }
        }
    }
}

@Composable
private fun FaqCard(item: FaqItem) {
    var expandido by remember { mutableStateOf(false) }
    val rotation by animateFloatAsState(targetValue = if (expandido) 180f else 0f, label = "rotation")

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expandido = !expandido },
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(Modifier.padding(Dimens.PaddingLarge)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = item.pergunta,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    Icons.Default.ExpandMore,
                    contentDescription = "Expandir",
                    modifier = Modifier.rotate(rotation)
                )
            }
            AnimatedVisibility(visible = expandido) {
                Column {
                    Divider(Modifier.padding(vertical = Dimens.PaddingMedium))
                    Text(
                        text = item.resposta,
                        style = MaterialTheme.typography.bodyLarge,
                        color = RktTextSecondary
                    )
                }
            }
        }
    }
}