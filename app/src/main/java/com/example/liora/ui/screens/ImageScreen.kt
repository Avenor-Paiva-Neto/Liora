package com.example.liora.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
 * Tela de Upload de Fotos do Liora, agora "desacoplada" e completa.
 */
@Composable
fun ImageScreen(
    imageUris: List<Uri?>,
    onImageSelected: (Uri?) -> Unit,
    onNavigateToNext: () -> Unit,
    onBackClick: () -> Unit
) {
    val requiredPhotos = 3

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri -> onImageSelected(uri) }
    )

    Scaffold(
        containerColor = BackgroundPrimary,
        topBar = {
            // Usando o TopBar com o contêiner cinza escuro, como no design.
            TopSectionContainer {
                ImageScreenTopBar(onBackClick = onBackClick)
            }
        },
        bottomBar = {
            PrimaryButton(
                text = "Próximo",
                onClick = onNavigateToNext,
                modifier = Modifier.padding(start = 24.dp, end = 24.dp, bottom = 16.dp, top = 8.dp),
                enabled = imageUris.count { it != null } >= requiredPhotos
            )
        }
    ) { innerPadding ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(start = 24.dp, end = 24.dp, top = 24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            horizontalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item(span = { GridItemSpan(2) }) {
                Column {
                    Text("Suas fotos", color = TextPrimary, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    Divider(color = TextSecondary.copy(alpha = 0.5f), thickness = 1.dp)
                    Spacer(Modifier.height(16.dp))
                    Text(
                        "Elas só serão exibidas após um metch com no mínimo 17 horas desde o início da conversa ou se você permitir antes",
                        color = TextSecondary, fontSize = 14.sp, lineHeight = 20.sp
                    )
                    Spacer(Modifier.height(32.dp))
                }
            }

            itemsIndexed(imageUris) { index, uri ->
                val isEnabled = (index == 0) || (imageUris.getOrNull(index - 1) != null)
                PhotoSlotCard(
                    uri = uri,
                    number = index + 1,
                    isEnabled = isEnabled,
                    onClick = {
                        if (isEnabled && uri == null) {
                            imagePickerLauncher.launch("image/*")
                        }
                    }
                )
            }
        }
    }
}

// =============================================================================
// COMPONENTES DESTA TELA - Incluídos aqui para garantir que nada quebre.
// =============================================================================

@Composable
fun TopSectionContainer(content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
            .background(BackgroundSecondary)
    ) {
        content()
    }
}

@Composable
fun ImageScreenTopBar(onBackClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 24.dp, end = 24.dp, top = 16.dp, bottom = 55.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(text = "SUA PERSONA", color = TextPrimary, fontSize = 29.sp, fontWeight = FontWeight.Bold)
            Text(text = "Adicione no mínimo 3 fotos", color = TextSecondary, fontSize = 14.sp)
        }
        Box(
            modifier = Modifier
                .size(60.dp)
                .clip(CircleShape)
                .background(AccentPrimary)
                .clickable(onClick = onBackClick),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Voltar", tint = AccentText)
        }
    }
}

@Composable
fun PhotoSlotCard(
    uri: Uri?,
    number: Int,
    isEnabled: Boolean,
    onClick: () -> Unit
) {
    val cardColor = Color.White
    val cardAlpha = if (isEnabled) 1f else 0.5f

    Card(
        modifier = Modifier
            .aspectRatio(0.7f)
            .clickable(enabled = isEnabled, onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor.copy(alpha = cardAlpha))
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            if (uri != null) {
                Image(
                    painter = rememberAsyncImagePainter(uri),
                    contentDescription = "Foto do perfil $number",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                Box(modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(colors = listOf(Color.Black.copy(alpha = 0.6f), Color.Transparent), startY = 0f, endY = 200f)))
            } else {
                if (isEnabled) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Adicionar foto",
                        tint = BackgroundPrimary.copy(alpha = 0.8f),
                        modifier = Modifier.size(56.dp)
                    )
                }
            }
            Text(
                text = number.toString(),
                color = if (uri != null) TextPrimary else BackgroundPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(12.dp)
            )
        }
    }
}


@Preview(showBackground = true)
@Composable
fun ImageScreenPreview() {
    LioraTheme {
        ImageScreen(
            imageUris = listOf(null, null, null, null, null, null),
            onImageSelected = {},
            onNavigateToNext = {},
            onBackClick = {}
        )
    }
}
