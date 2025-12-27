package com.example.liora.ui.conversations

import android.app.Application
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed // ALTERADO: para itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.liora.R
import com.example.liora.di.AppViewModelFactory
import com.example.liora.domain.model.UserProfile
import com.example.liora.ui.AppRoutes
import kotlinx.coroutines.delay

val OpenSans = FontFamily(
    Font(R.font.open_sans, FontWeight.Normal),
    Font(R.font.open_sans_bold, FontWeight.Bold)
)

// =================================================================================
// ADICIONADO: MODIFIER DE ANIMAÇÃO ESCALONADA
// =================================================================================
fun Modifier.staggeredEnterAnimation(
    index: Int,
    durationMillis: Int = 500,
    delayPerItemMillis: Int = 100
): Modifier = composed {
    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(index.toLong() * delayPerItemMillis)
        isVisible = true
    }

    val alpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(durationMillis = durationMillis),
        label = "AlphaAnimation"
    )

    val translationY by animateDpAsState(
        targetValue = if (isVisible) 0.dp else 30.dp,
        animationSpec = tween(durationMillis = durationMillis),
        label = "TranslationYAnimation"
    )

    this.graphicsLayer {
        this.alpha = alpha
        this.translationY = translationY.toPx()
    }
}

@Composable
fun ConversationsScreen(
    navController: NavController
) {
    val application = LocalContext.current.applicationContext as Application
    val viewModel: ConversationsViewModel = viewModel(factory = AppViewModelFactory(application))
    val uiState by viewModel.uiState.collectAsState()

    var searchText by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("Principal") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(32.dp))

        // --- CABEÇALHO: Título e Seta ---
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "CONVERSAS",
                fontFamily = OpenSans,
                fontWeight = FontWeight.Bold,
                fontSize = 36.sp,
                color = Color.White
            )
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                contentDescription = "Voltar",
                tint = Color.White,
                modifier = Modifier
                    .size(36.dp)
                    .clickable { navController.popBackStack() }
            )
        }

        Spacer(modifier = Modifier.height(48.dp))

        // --- BOTÕES DE FILTRO ---
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            FilterButton(text = "Principal", isSelected = selectedFilter == "Principal", selectedColor = Color(0xFF6A3E8A)) { selectedFilter = "Principal" }
            FilterButton(text = "Pedidos", isSelected = selectedFilter == "Pedidos", selectedColor = Color(0xFF6E5599)) { selectedFilter = "Pedidos" }
            FilterButton(text = "Arquivo", isSelected = selectedFilter == "Arquivo", selectedColor = Color(0xFF004AAD)) { selectedFilter = "Arquivo" }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // --- BARRA DE PESQUISA ---
        TextField(
            value = searchText,
            onValueChange = { searchText = it },
            placeholder = { Text("Procurar", color = Color.Gray, fontFamily = OpenSans) },
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = "Pesquisar", tint = Color.Gray) },
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF1C1C1E)),
            colors = TextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                cursorColor = Color.White,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent,
                errorIndicatorColor = Color.Transparent
            ),
            singleLine = true,
            textStyle = TextStyle(fontFamily = OpenSans, color = Color.White)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // --- LISTA DE CONVERSAS ---
        when (val state = uiState) {
            is ConversationsUiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color.White)
                }
            }
            is ConversationsUiState.Error -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = "Erro: ${state.message}", color = MaterialTheme.colorScheme.error)
                }
            }
            is ConversationsUiState.Success -> {
                if (state.conversations.isEmpty()) {
                    // ... (código para lista vazia)
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(top = 8.dp)
                    ) {
                        // ALTERADO: Usando itemsIndexed para obter o índice de cada item
                        itemsIndexed(
                            items = state.conversations,
                            key = { _, item -> item.userProfile.uid } // Chave para melhor performance
                        ) { index, conversationListItem ->
                            val hasUnreadMessage = conversationListItem.userProfile.name.contains("Ana")

                            MatchedUserItem(
                                // APLICADO: O novo modifier de animação é passado aqui
                                modifier = Modifier.staggeredEnterAnimation(index = index),
                                conversationListItem = conversationListItem,
                                hasUnreadMessage = hasUnreadMessage,
                            ) {
                                navController.navigate("${AppRoutes.INDIVIDUAL_CHAT}/${conversationListItem.userProfile.uid}")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FilterButton(text: String, isSelected: Boolean, selectedColor: Color, onClick: () -> Unit) {
    val targetBackgroundColor = if (isSelected) selectedColor else Color(0xFF2C2C2E)
    val animatedBackgroundColor by animateColorAsState(
        targetValue = targetBackgroundColor,
        animationSpec = tween(durationMillis = 300)
    )

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(animatedBackgroundColor)
            .clickable(onClick = onClick)
            .padding(horizontal = 24.dp, vertical = 12.dp)
    ) {
        Text(
            text = text,
            color = Color.White,
            fontFamily = OpenSans,
            fontWeight = FontWeight.Medium,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
fun MatchedUserItem(
    // ALTERADO: Adicionado o modifier como parâmetro
    modifier: Modifier = Modifier,
    conversationListItem: ConversationListItem,
    hasUnreadMessage: Boolean,
    onItemClick: (ConversationListItem) -> Unit
) {
    val userProfile = conversationListItem.userProfile
    val imageDisplayInfo = conversationListItem.imageDisplayInfo

    Row(
        // APLICADO: Modifier recebido como parâmetro
        modifier = modifier
            .fillMaxWidth()
            .clickable { onItemClick(conversationListItem) }
            .padding(vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = imageDisplayInfo.imageUrl,
            contentDescription = "Foto de perfil de ${userProfile.name}",
            modifier = Modifier
                .size(60.dp)
                .clip(CircleShape)
                .then(
                    if (imageDisplayInfo.shouldBlur) Modifier.blur(12.dp)
                    else Modifier
                ),
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = userProfile.name,
                style = MaterialTheme.typography.titleMedium,
                fontFamily = OpenSans,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = if (hasUnreadMessage) "Kk voce acha mesmo ..." else "Visto a 3h",
                style = MaterialTheme.typography.bodyMedium,
                fontFamily = OpenSans,
                color = if (hasUnreadMessage) Color.White else Color.LightGray,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        if (hasUnreadMessage) {
            Spacer(modifier = Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE91E63))
            )
        }
    }
}