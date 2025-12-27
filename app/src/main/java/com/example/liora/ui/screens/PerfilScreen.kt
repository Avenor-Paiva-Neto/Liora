package com.example.liora.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.liora.ui.theme.*

// --- DADOS SIMULADOS (Enriquecidos com a personalidade) ---

// Modelo para os traços de personalidade
data class PsycheTrait(val icon: String, val text: String)

// Modelo de Perfil de Usuário completo
data class UserProfile(
    val name: String,
    val age: Int,
    val bio: String,
    val profilePictureUrl: String,
    val galleryImageUrls: List<String>,
    val traits: List<PsycheTrait> // O "DNA Liora"
)

// Nosso usuário de simulação
val mockProfile = UserProfile(
    name = "Maria Lyla",
    age = 28,
    bio = "Sou uma pessoa criativa, trabalho muito em meus projetos e não gosto de dependência emocional. Curto games, uma boa conversa e paz.",
    profilePictureUrl = "https://images.pexels.com/photos/2811089/pexels-photo-2811089.jpeg",
    galleryImageUrls = listOf(
        "https://picsum.photos/seed/1/400/600",
        "https://picsum.photos/seed/6/400/600",
        "https://picsum.photos/seed/4/400/600",
        "https://picsum.photos/seed/9/400/600",
        "https://picsum.photos/seed/8/400/600"
    ),
    traits = listOf(
        PsycheTrait("🎨", "Criativa"),
        PsycheTrait("🎮", "Gamer"),
        PsycheTrait("🧘", "Paz Interior"),
        PsycheTrait("✈️", "Viajante"),
        PsycheTrait("💼", "Focada na Carreira")
    )
)

// --- O COMPOSABLE PRINCIPAL (Stateful) ---

@Composable
fun PerfilScreen(
    // No futuro, receberá o ID do usuário e o ViewModel
    // userId: String,
    // viewModel: ProfileViewModel
) {
    // Estado para simular a troca entre "Meu Perfil" e "Perfil de Visitante"
    var isMyProfile by remember { mutableStateOf(true) }

    // O estado do scroll para a animação Parallax
    val scrollState = rememberScrollState()

    Scaffold(
        containerColor = BackgroundPrimary,
        floatingActionButton = {
            // O FAB de Edição só aparece se for o meu perfil
            if (isMyProfile) {
                FloatingActionButton(
                    onClick = { /* TODO: Lógica de Edição */ },
                    containerColor = AccentPrimary,
                    shape = CircleShape
                ) {
                    Icon(Icons.Default.Edit, contentDescription = "Editar Perfil", tint = AccentText)
                }
            }
        }
    ) { paddingValues ->


        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {

        ProfileHeroHeader(
                imageUrl = mockProfile.profilePictureUrl,
                scrollOffset = scrollState.value.toFloat()
            )

            // --- CAMADA 2: O CONTEÚDO ROLÁVEL ---
            Column(modifier = Modifier.verticalScroll(scrollState)) {
                // Espaçador para empurrar o conteúdo para baixo do Header
                Spacer(modifier = Modifier.height(300.dp))

                // Conteúdo principal do perfil
                ProfileContent(
                    profile = mockProfile,
                    isMyProfile = isMyProfile
                )

                // Botão de simulação (REMOVER EM PRODUÇÃO)
                // Apenas para testar a troca de visualização
                Switch(
                    checked = isMyProfile,
                    onCheckedChange = { isMyProfile = it },
                    modifier = Modifier.padding(16.dp).align(Alignment.CenterHorizontally)
                )
                Text(
                    text = if(isMyProfile) "Vendo meu perfil" else "Vendo como visitante",
                    color = TextSecondary,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
        }
    }
}

// --- SUB-COMPONENTES MODULARES ---

@Composable
private fun ProfileHeroHeader(imageUrl: String, scrollOffset: Float) {
    // Animação para o efeito de fade out conforme rola a tela
    val imageAlpha by animateFloatAsState(
        targetValue = (1f - (scrollOffset / 600f)).coerceIn(0f, 1f),
        label = "alpha"
    )

    AsyncImage(
        model = imageUrl,
        contentDescription = "Foto de perfil",
        contentScale = ContentScale.Crop,
        modifier = Modifier
            .fillMaxWidth()
            .height(350.dp)
            .graphicsLayer {
                // Efeito Parallax: move a imagem para baixo na metade da velocidade do scroll
                translationY = scrollOffset * 0.5f
                // Efeito de Fade Out
                alpha = imageAlpha
            }
    )
    // Gradiente para suavizar a transição entre a imagem e o conteúdo
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(350.dp)
            .background(
                Brush.verticalGradient(
                    listOf(Color.Transparent, Color.Transparent, BackgroundPrimary),
                    startY = 0f,
                    endY = 800f
                )
            )
    )
}

@Composable
private fun ProfileContent(profile: UserProfile, isMyProfile: Boolean) {
    Column(
        modifier = Modifier
            .background(BackgroundPrimary) // Garante que o fundo cubra a imagem ao rolar
            .padding(top = 16.dp)
    ) {
        // Nome e Idade
        Text(
            text = "${profile.name}, ${profile.age}",
            color = TextPrimary,
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 24.dp)
        )
        Spacer(modifier = Modifier.height(24.dp))

        // Seção "DNA Liora" - O GRANDE DIFERENCIAL
        DnaLioraSection(traits = profile.traits)
        Spacer(modifier = Modifier.height(32.dp))

        // Seção da Bio
        BioSection(bio = profile.bio)
        Spacer(modifier = Modifier.height(32.dp))

        // Seção da Galeria
        PhotoGallerySection(photos = profile.galleryImageUrls, isMyProfile = isMyProfile)
    }
}

@Composable
private fun DnaLioraSection(traits: List<PsycheTrait>) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "DNA LIORA",
            color = TextSecondary,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(horizontal = 24.dp)
        )
        Spacer(modifier = Modifier.height(12.dp))
        LazyRow(
            contentPadding = PaddingValues(horizontal = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(traits) { trait ->
                TraitChip(trait = trait)
            }
        }
    }
}

@Composable
private fun TraitChip(trait: PsycheTrait) {
    Surface(
        color = ChipBackground,
        shape = RoundedCornerShape(50)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = trait.icon, fontSize = 16.sp)
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = trait.text, color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
        }
    }
}

@Composable
private fun BioSection(bio: String) {
    Column(modifier = Modifier.padding(horizontal = 24.dp)) {
        Text(
            text = "SOBRE MIM",
            color = TextSecondary,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(text = bio, color = TextPrimary, fontSize = 16.sp, lineHeight = 24.sp)
    }
}

@Composable
private fun PhotoGallerySection(photos: List<String>, isMyProfile: Boolean) {
    Column(modifier = Modifier.padding(horizontal = 24.dp)) {
        Text(
            text = "GALERIA",
            color = TextSecondary,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Grade "Staggered" (escalonada) com 2 colunas
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            // Coluna 1
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                photos.forEachIndexed { index, url ->
                    if (index % 2 == 0) { // Itens pares
                        GalleryImage(url = url)
                    }
                }
            }
            // Coluna 2
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                photos.forEachIndexed { index, url ->
                    if (index % 2 != 0) { // Itens ímpares
                        GalleryImage(url = url)
                    }
                }
                // Botão de adicionar só aparece na coluna mais curta, se for o meu perfil
                if(isMyProfile && photos.size < 6) {
                    AddPhotoCard()
                }
            }
        }
    }
}

@Composable
fun GalleryImage(url: String) {
    AsyncImage(
        model = url,
        contentDescription = "Foto da galeria",
        contentScale = ContentScale.Crop,
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.75f)
            .clip(RoundedCornerShape(16.dp))
    )
}

@Composable
fun AddPhotoCard() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.75f)
            .clip(RoundedCornerShape(16.dp))
            .background(ChipBackground)
            .clickable { /* TODO */ },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = "Adicionar foto",
            tint = TextSecondary,
            modifier = Modifier.size(48.dp)
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
fun PerfilScreenPreview() {
    LioraTheme {
        PerfilScreen()
    }
}