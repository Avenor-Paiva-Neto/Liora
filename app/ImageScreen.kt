package com.example.liora.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.liora.ui.theme.*

/**
 * Tela de Upload de Fotos do Liora.
 * Inclui lógica para seleção de imagens e upload sequencial.
 */
@Composable
fun ImageScreen(
    // No futuro, receberíamos o viewModel e a ação de navegar
) {
    // --- Gerenciamento de Estado ---
    // Criamos uma lista para guardar as URIs das imagens selecionadas.
    // O tamanho é 6, e todos começam como nulos.
    val imageUris = remember { mutableStateListOf<Uri?>(*Array(6) { null }) }
    val requiredPhotos = 3

    // --- Lógica do Seletor de Imagens ---
    // `rememberLauncherForActivityResult` é a forma moderna de pedir um resultado,
    // como uma imagem da galeria.
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            // Encontra o primeiro slot vazio e insere a nova imagem
            val emptyIndex = imageUris.indexOfFirst { it == null }
            if (emptyIndex != -1) {
                imageUris[emptyIndex] = it
            }
        }
    }

    Scaffold(
        containerColor = BackgroundPrimary,
        topBar = {
            // Usamos o AppTopBar que já temos, adaptando o conteúdo.
            AppTopBar(
                title = "SUA PERSONA",
                subtitle = "Adicione no mínimo 3 fotos",
                onBackClick = { /* TODO */ }
            )
        },
        bottomBar = {
            // O botão só fica habilitado quando o número mínimo de fotos for atingido.
            PrimaryButton(
                text = "Próximo",
                onClick = { /* TODO: Navegar */ },
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
                enabled = imageUris.count { it != null } >= requiredPhotos
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp)
        ) {
            SectionTitle(title = "Suas fotos")
            Text(
                // Usando a frase refinada que discutimos
                text = "Acreditamos que conexões reais vão além da primeira foto. Suas fotos só serão reveladas mutuamente após uma conexão ser construída.",
                color = TextSecondary,
                fontSize = 14.sp
            )
            Spacer(Modifier.height(24.dp))

            // --- Grid de Fotos ---
            // `LazyVerticalGrid` é a melhor forma de criar um grid rolável.
            LazyVerticalGrid(
                columns = GridCells.Fixed(2), // 2 colunas
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                itemsIndexed(imageUris) { index, uri ->
                    // A lógica de habilitação garante o upload sequencial.
                    val isEnabled = (index == 0) || (imageUris[index - 1] != null)

                    PhotoSlotCard(
                        uri = uri,
                        number = index + 1,
                        isEnabled = isEnabled,
                        onClick = {
                            if (isEnabled) {
                                // Lança o seletor de imagens
                                imagePickerLauncher.launch("image/*")
                            }
                        }
                    )
                }
            }
        }
    }
}


// =============================================================================
// COMPONENTES DESTA TELA - Fiéis ao seu design
// =============================================================================

@Composable
fun PhotoSlotCard(
    uri: Uri?,
    number: Int,
    isEnabled: Boolean,
    onClick: () -> Unit
) {
    // Usamos um Card como base, que já tem elevação e forma.
    Card(
        modifier = Modifier
            .aspectRatio(0.75f) // Proporção para o card ficar mais retangular
            .clickable(enabled = isEnabled, onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        // Box nos permite empilhar elementos (a imagem sobre o gradiente).
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            // Se a URI não for nula, mostramos a imagem.
            if (uri != null) {
                Image(
                    // Usamos a biblioteca Coil para carregar a imagem de forma assíncrona.
                    // Adicione `implementation("io.coil-kt:coil-compose:2.5.0")` no seu build.gradle.kts.
                    painter = rememberAsyncImagePainter(uri),
                    contentDescription = "Foto do perfil $number",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop // Garante que a imagem preencha o card
                )
            } else {
                // Se for nula, mostramos o placeholder com o gradiente e o "+".
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color(0xFFE0E0E0), Color.Black),
                            )
                        )
                )
                // O ícone só aparece se o slot estiver habilitado.
                if (isEnabled) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Adicionar foto",
                        tint = TextPrimary,
                        modifier = Modifier.size(48.dp)
                    )
                }
            }

            // O número do slot, no canto superior esquerdo.
            Text(
                text = number.toString(),
                color = TextPrimary.copy(alpha = if(uri != null) 0.8f else 1.0f),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(8.dp)
            )
        }
    }
}


@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
fun ImageScreenPreview() {
    LioraTheme {
        ImageScreen()
    }
}
