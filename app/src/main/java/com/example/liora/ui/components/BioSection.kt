package com.example.liora.ui.discovery.components

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Cor movida para o contexto do componente ou um arquivo de Tema,
// mas por enquanto definimos aqui para manter a autossuficiência.
private val InfoCardBackgroundColor = Color(0xFF1E1E1E)

@Composable
fun BioSection(
    bio: String,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = InfoCardBackgroundColor)
        ) {
            Column(
                modifier = Modifier
                    .animateContentSize(animationSpec = tween(durationMillis = 300))
                    .padding(start = 16.dp, end = 16.dp, top = 24.dp, bottom = 16.dp)
            ) {
                Text(
                    text = bio,
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.8f),
                    maxLines = if (isExpanded) Int.MAX_VALUE else 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 20.sp
                )
                Text(
                    text = if (isExpanded) "LER MENOS" else "LER MAIS",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = Color.White,
                    modifier = Modifier
                        .align(Alignment.End)
                        .padding(top = 8.dp)
                        .clickable(onClick = onToggle)
                )
            }
        }
        Text(
            text = "BIO",
            fontWeight = FontWeight.Bold,
            fontSize = 27.sp,
            color = Color.White,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = 16.dp)
        )
    }
}