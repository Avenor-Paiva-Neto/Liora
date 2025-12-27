package com.example.liora.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.liora.ui.theme.*

// --- DADOS SIMULADOS (sem alterações) ---

enum class ConversationCategory {
    PRINCIPAL, PEDIDOS, ARQUIVO
}

data class StoryProfile(
    val id: Int,
    val name: String,
    val imageUrl: String
)

data class Conversation(
    val id: Int,
    val name: String,
    val lastMessage: String,
    val timestamp: String,
    val unread: Boolean,
    val imageUrl: String,
    val category: ConversationCategory
)

val mockStories = listOf(
    StoryProfile(1, "Meu nome", ""),
    StoryProfile(2, "Josep pinto", "https://i.imgur.com/34s4s44.jpeg"),
    StoryProfile(3, "Anele tronic", "https://i.imgur.com/W2CmMw6.jpeg"),
    StoryProfile(4, "Outro Nome", "https://i.imgur.com/gCeS81E.jpeg")
)

val mockConversations = listOf(
    Conversation(1, "Flavia dias", "Visto a 3h", "3h", false, "https://i.imgur.com/34s4s44.jpeg", ConversationCategory.PRINCIPAL),
    Conversation(2, "Ana treinberg", "Kk voce acha mesmo ...", "1h", true, "https://i.imgur.com/W2CmMw6.jpeg", ConversationCategory.PRINCIPAL),
    Conversation(3, "Josep pinto", "Visto a 6h", "6h", false, "https://i.imgur.com/gCeS81E.jpeg", ConversationCategory.PRINCIPAL),
    Conversation(4, "Anele tronic", "Visto a 0m", "0m", false, "https://i.imgur.com/W2CmMw6.jpeg", ConversationCategory.PRINCIPAL),
    Conversation(5, "Carlos (Pedido)", "Oi, tudo bem? Vi seu perfil...", "1d", true, "https://i.imgur.com/34s4s44.jpeg", ConversationCategory.PEDIDOS),
    Conversation(6, "Mariana (Pedido)", "Acho que temos muito em comum!", "2d", false, "https://i.imgur.com/gCeS81E.jpeg", ConversationCategory.PEDIDOS),
    Conversation(7, "Ex-Conversa", "Ok, obrigado!", "3sem", false, "https://i.imgur.com/W2CmMw6.jpeg", ConversationCategory.ARQUIVO)
)


// --- COMPOSABLE PRINCIPAL (Stateful) ---

@Composable
fun ChatScreen() {
    val conversationsState = remember { mutableStateOf(mockConversations) }
    var selectedTab by remember { mutableStateOf(ConversationCategory.PRINCIPAL) }

    val filteredConversations = remember(selectedTab, conversationsState.value) {
        conversationsState.value.filter { it.category == selectedTab }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF121212)) // Cor de fundo geral da tela
    ) {
        TopBarComAbas(
            selectedTab = selectedTab,
            onTabSelected = { newTab -> selectedTab = newTab },
            onBackClick = { /* TODO: Implementar ação de voltar */ }
        )

        Spacer(modifier = Modifier.height(24.dp))
        StoriesSection() // Componente com o tamanho das fotos alterado
        Divider(color = Color.Gray.copy(alpha = 0.2f), modifier = Modifier.padding(vertical = 24.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            items(items = filteredConversations, key = { it.id }) { conversation ->
                ConversationItem( // Componente com o tamanho da foto alterado
                    conversation = conversation,
                    onRemove = {
                        conversationsState.value = conversationsState.value - conversation
                    }
                )
            }
        }
    }
}


// --- COMPONENTES ATUALIZADOS ---

@Composable
private fun TopBarComAbas(
    selectedTab: ConversationCategory,
    onTabSelected: (ConversationCategory) -> Unit,
    onBackClick: () -> Unit
) {
    // ▼▼▼ ALTERAÇÃO 1: Degradê de preto para cinza escuro ▼▼▼
    val gradientBrush = Brush.horizontalGradient(
        colors = listOf(Color(0xFF000000), Color(0xFF404040))
    )

    val containerShape = RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(containerShape)
            .background(brush = gradientBrush)
            // ▼▼▼ ALTERAÇÃO 2: Aumento do espaçamento vertical ▼▼▼
            .padding(top = 24.dp, bottom = 24.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 24.dp, end = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "CONVERSAS",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold
            )
            IconButton(
                onClick = onBackClick,
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    // Cor de destaque ajustada para o novo tema
                    .background(Color(0xFF3A3A3A))
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Voltar",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        // ▼▼▼ ALTERAÇÃO 2: Aumento do espaçamento vertical ▼▼▼
        Spacer(modifier = Modifier.height(20.dp))

        val tabs = ConversationCategory.values()
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            tabs.forEach { tab ->
                val isSelected = tab == selectedTab
                // Cores ajustadas para o novo tema
                val containerColor = if (isSelected) Color(0xFFE91E63) else Color(0xFF2E2E2E)
                val textColor = if (isSelected) Color.White else Color.Gray

                Button(
                    onClick = { onTabSelected(tab) },
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(containerColor = containerColor),
                    modifier = Modifier.height(40.dp),
                    elevation = null
                ) {
                    Text(
                        text = tab.name.replaceFirstChar { it.titlecase() },
                        color = textColor,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun StoriesSection() {
    val context = LocalContext.current
    LazyRow(
        contentPadding = PaddingValues(horizontal = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        items(items = mockStories, key = { it.id }) { story ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.clickable {
                    Toast.makeText(context, "O Story estará disponível apenas para assinantes no futuro", Toast.LENGTH_SHORT).show()
                }
            ) {
                if (story.id == 1) {
                    Box(
                        // ▼▼▼ ALTERAÇÃO 3: Aumento do tamanho do box "Adicionar Story" ▼▼▼
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(Color.DarkGray.copy(alpha = 0.5f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Adicionar Story", tint = Color.LightGray, modifier = Modifier.size(40.dp))
                    }
                } else {
                    AsyncImage(
                        model = story.imageUrl,
                        contentDescription = "Story de ${story.name}",
                        contentScale = ContentScale.Crop,
                        // ▼▼▼ ALTERAÇÃO 3: Aumento do tamanho da foto do story ▼▼▼
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .border(3.dp, Color(0xFF4A4A4A), CircleShape) // Borda ajustada
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = story.name, color = TextSecondary, fontSize = 12.sp)
            }
        }
    }
}

@Composable
private fun ConversationItem(
    conversation: Conversation,
    onRemove: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { /* TODO: Navegar para o chat individual */ }
            .padding(horizontal = 24.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box {
            AsyncImage(
                model = conversation.imageUrl,
                contentDescription = "Foto de ${conversation.name}",
                contentScale = ContentScale.Crop,
                // ▼▼▼ ALTERAÇÃO 4: Aumento do tamanho da foto da mensagem ▼▼▼
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
            )
            if (conversation.unread) {
                Box(
                    modifier = Modifier
                        .size(14.dp) // Ponto de notificação um pouco maior
                        .background(Color(0xFFE91E63), CircleShape)
                        .border(2.dp, Color(0xFF121212), CircleShape)
                        .align(Alignment.BottomEnd)
                )
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(text = conversation.name, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = conversation.lastMessage, color = TextSecondary, fontSize = 14.sp, maxLines = 1)
        }

        Spacer(modifier = Modifier.width(16.dp))

        IconButton(onClick = onRemove) {
            Icon(Icons.Default.Close, contentDescription = "Remover conversa", tint = TextSecondary)
        }
    }
}


@Preview(showBackground = true, backgroundColor = 0xFF121212)
@Composable
fun ChatScreenPreview() {
    // Use um tema escuro se tiver, senão defina as cores manualmente como feito acima
    ChatScreen()
}