package com.example.liora.ui.discovery.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun EmptyProfileMessage(onUndo: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Nenhum novo perfil encontrado.", textAlign = TextAlign.Center, color = Color.Gray)
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onUndo) {
            Icon(imageVector = Icons.Default.Replay, contentDescription = "Desfazer")
            Spacer(modifier = Modifier.width(8.dp))
            Text("Desfazer Última Ação")
        }
    }
}