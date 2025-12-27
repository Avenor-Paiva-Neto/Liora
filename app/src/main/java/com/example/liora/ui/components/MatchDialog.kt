package com.example.liora.ui.discovery.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.rememberAsyncImagePainter
import com.example.liora.domain.model.UserProfile

@Composable
fun MatchDialog(
    currentUserProfile: UserProfile,
    matchedUser: UserProfile,
    onDismissRequest: () -> Unit,
    onSendMessageRequest: () -> Unit
) {
    Dialog(onDismissRequest = onDismissRequest) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF2C2C2E))
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(text = "IT'S A MATCH!", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = Color.White, letterSpacing = 2.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "Você e ${matchedUser.name} se curtiram!", fontSize = 16.sp, color = Color.LightGray, textAlign = TextAlign.Center)
                Spacer(modifier = Modifier.height(24.dp))
                Box(modifier = Modifier.height(100.dp), contentAlignment = Alignment.Center) {
                    MatchProfilePicture(
                        imageUrl = matchedUser.imageUrls.firstOrNull(),
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .size(100.dp)
                    )
                    MatchProfilePicture(
                        imageUrl = currentUserProfile.imageUrls.firstOrNull(),
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .size(100.dp)
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = onSendMessageRequest,
                    modifier = Modifier.fillMaxWidth(),
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE91E63))
                ) {
                    Text("ENVIAR UMA MENSAGEM", modifier = Modifier.padding(vertical = 8.dp))
                }
                Spacer(modifier = Modifier.height(8.dp))
                TextButton(onClick = onDismissRequest) {
                    Text("CONTINUAR DESCOBRINDO", color = Color.Gray)
                }
            }
        }
    }
}

@Composable
private fun MatchProfilePicture(imageUrl: String?, modifier: Modifier = Modifier) {
    Image(
        painter = rememberAsyncImagePainter(model = imageUrl),
        contentDescription = "Foto de perfil do match",
        contentScale = ContentScale.Crop,
        modifier = modifier
            .clip(CircleShape)
            .border(3.dp, Color(0xFFE91E63), CircleShape)
    )
}