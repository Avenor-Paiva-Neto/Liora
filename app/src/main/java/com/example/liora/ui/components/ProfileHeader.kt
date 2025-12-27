package com.example.liora.ui.discovery.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.liora.R // <<-- 1. IMPORTANTE: VERIFIQUE SE O IMPORT DA CLASSE R ESTÁ CORRETO
import com.example.liora.ui.conversations.ConversationListItem
import com.example.liora.ui.discovery.DiscoveryAction
import com.example.liora.ui.discovery.ProfileActions
import java.util.Calendar
import androidx.compose.foundation.ExperimentalFoundationApi

// --- Cores e Constantes ---
private val TopButtonColor = Color.Black.copy(alpha = 0.3f)
private val SubtleTextColor = Color(0xFFa6a6a6)

// <<-- 2. DEFINIÇÃO DA FAMÍLIA DE FONTES OPEN SANS
val OpenSans = FontFamily(
    Font(R.font.open_sans, FontWeight.Normal),
    Font(R.font.open_sans_bold, FontWeight.Bold)
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ProfileHeader(
    conversationListItem: ConversationListItem,
    screenWidth: Dp,
    onAction: (DiscoveryAction) -> Unit,
    modifier: Modifier = Modifier
) {
    val userProfile = conversationListItem.userProfile
    val imageDisplayInfo = conversationListItem.imageDisplayInfo

    val pagerState = rememberPagerState(pageCount = { userProfile.imageUrls.size })

    Box(
        modifier = modifier
            .padding(top = 5.dp, start = 5.dp, end = 5.dp)
            .fillMaxSize()
            .clip(RoundedCornerShape(size = 16.dp)) // Borda ajustada
    ) {
        HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { pageIndex ->
            val imageUrl = userProfile.imageUrls.getOrNull(pageIndex)
            if (imageUrl != null) {
                Image(
                    painter = rememberAsyncImagePainter(imageUrl),
                    contentDescription = "Foto de Perfil ${pageIndex + 1}",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .then(
                            if (imageDisplayInfo.shouldBlur) Modifier.blur(12.dp)
                            else Modifier
                        )
                )
            } else {
                Box(Modifier.fillMaxSize().background(Color.Gray), contentAlignment = Alignment.Center) {
                    Text("No Image", color = Color.White)
                }
            }
        }

        Box(modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(colorStops = arrayOf(0.5f to Color.Transparent, 1.0f to Color.Black))))

        if (userProfile.imageUrls.size > 1) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
                    .align(Alignment.TopCenter),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                userProfile.imageUrls.forEachIndexed { index, _ ->
                    PageIndicator(
                        isSelected = index == pagerState.currentPage
                    )
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TopActionButton(onClick = { onAction(DiscoveryAction.Undo) }, icon = Icons.Default.Replay, text = "VOLTAR")
                TopActionButton(onClick = { onAction(DiscoveryAction.Report) }, icon = Icons.Default.WarningAmber, text = "REPORT")
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f, fill = false)) {
                    val age = Calendar.getInstance().get(Calendar.YEAR) - userProfile.birthYear
                    val nameFontSize = if (screenWidth < 360.dp) 35.sp else 39.sp
                    val displayName = if (userProfile.name.contains(" ")) userProfile.name.replaceFirst(" ", "\n") else userProfile.name

                    // <<-- 3. APLICAÇÃO DA FONTE NO TEXTO DE NOME E IDADE
                    Text(
                        text = "$displayName, $age",
                        color = Color.White,
                        fontSize = nameFontSize,
                        fontFamily = OpenSans, // Fonte aplicada
                        fontWeight = FontWeight.Bold,
                        lineHeight = nameFontSize * 1.15f,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(Modifier.height(4.dp))

                    // <<-- 4. APLICAÇÃO DA FONTE NO TEXTO DE DISTÂNCIA (OPCIONAL, MAS RECOMENDADO)
                    Text(
                        text = "A 30 km de você",
                        color = SubtleTextColor,
                        fontSize = 16.sp,
                        fontFamily = OpenSans, // Fonte aplicada
                        fontWeight = FontWeight.Normal
                    )
                }
                Spacer(Modifier.width(8.dp))
                ProfileActions(onAction = onAction, screenWidth = screenWidth)
            }
        }
    }
}

@Composable
private fun PageIndicator(isSelected: Boolean) {
    val size = 8.dp
    val color = if (isSelected) Color.White else Color.White.copy(alpha = 0.5f)
    val shape = CircleShape

    Box(
        modifier = Modifier
            .padding(horizontal = 4.dp)
            .size(size)
            .clip(shape)
            .background(color)
    )
}

@Composable
private fun TopActionButton(
    onClick: () -> Unit,
    icon: ImageVector,
    text: String
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = TopButtonColor),
        shape = CircleShape,
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Icon(imageVector = icon, contentDescription = text, tint = Color.White, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(8.dp))
        // Apliquei a fonte aqui também para manter a consistência visual
        Text(
            text = text,
            fontFamily = OpenSans, // <<-- 5. FONTE NOS BOTÕES
            fontWeight = FontWeight.Bold,
            color = Color.White,
            fontSize = 14.sp
        )
    }
}