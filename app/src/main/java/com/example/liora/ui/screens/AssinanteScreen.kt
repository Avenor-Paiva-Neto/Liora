package com.example.liora.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Definição das cores para corresponder à identidade visual das imagens
val LioraPink = Color(0xFFE91E63)
val LioraDarkBackground = Color.Black
val BackgroundSecondary = Color(0xFF1C1C1E) // Nova cor para os cards
val LioraLightText = Color.White
val LioraGrayText = Color.LightGray

@Composable
fun AssinanteScreen(onBackClicked: () -> Unit = {}) {
    Scaffold(
        topBar = {
            AssinanteTopBar(onBackClicked)
        },
        containerColor = LioraDarkBackground
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                Spacer(modifier = Modifier.height(20.dp))
                PipocaMediaPlan()
                Spacer(modifier = Modifier.height(24.dp))
                PipocaGrandePlan() // Plano "Pipoca grande" agora com card
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssinanteTopBar(onBackClicked: () -> Unit) {
    TopAppBar(
        title = {
            Text(
                "PLANOS",
                fontWeight = FontWeight.Bold,
                color = LioraLightText,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        },
        navigationIcon = {
            Box(
                modifier = Modifier
                    .padding(start = 8.dp)
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(BackgroundSecondary.copy(alpha = 0.8f)),
                contentAlignment = Alignment.Center
            ) {
                IconButton(onClick = onBackClicked) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Voltar",
                        tint = LioraLightText
                    )
                }
            }
        },
        actions = {
            Spacer(modifier = Modifier.width(48.dp))
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Transparent
        )
    )
}

@Composable
fun PipocaMediaPlan() {
    Surface(
        color = BackgroundSecondary, // Cor do card atualizada
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(vertical = 24.dp, horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Pipoca media",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = LioraLightText
            )
            Spacer(modifier = Modifier.height(24.dp))
            PriceText("6,99")
            Spacer(modifier = Modifier.height(24.dp))

            FeatureItem(
                icon = Icons.Default.Refresh,
                description = buildAnnotatedString {
                    append("Retornar ao perfil anterior ")
                    withStyle(style = SpanStyle(color = LioraPink, fontWeight = FontWeight.Bold)) {
                        append("10 vezes")
                    }
                    append(" ao dia")
                }
            )
            Spacer(modifier = Modifier.height(16.dp))
            FeatureItem(
                icon = Icons.Default.Favorite,
                description = buildAnnotatedString { append("Ver os usuários que te curtiram") }
            )
            Spacer(modifier = Modifier.height(16.dp))
            FeatureItem(
                icon = Icons.Default.Chat,
                description = buildAnnotatedString {
                    append("Mandar até ")
                    withStyle(style = SpanStyle(color = LioraPink, fontWeight = FontWeight.Bold)) {
                        append("5 mensagem")
                    }
                    append(" adiantadas")
                }
            )
            Spacer(modifier = Modifier.height(16.dp))
            FeatureItem(
                icon = Icons.Default.Person,
                description = buildAnnotatedString {
                    append("Visualize até ")
                    withStyle(style = SpanStyle(color = LioraPink, fontWeight = FontWeight.Bold)) {
                        append("50 perfis")
                    }
                    append(" por dia")
                }
            )
            Spacer(modifier = Modifier.height(32.dp))
            AssinarButton()
        }
    }
}

@Composable
fun PipocaGrandePlan() {
    // MODIFICAÇÃO: Adicionado o Surface para criar o card de fundo
    Surface(
        color = BackgroundSecondary, // Cor do card atualizada
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(vertical = 24.dp, horizontal = 16.dp)
        ) {
            Text(
                text = "Pipoca grande",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = LioraLightText,
            )
            Spacer(modifier = Modifier.height(16.dp))
            PriceText("14,99")
            Spacer(modifier = Modifier.height(24.dp))

            Column(modifier = Modifier.padding(horizontal = 0.dp)) { // Padding interno removido pois o card já tem
                FeatureItem(
                    icon = Icons.Default.PhotoCamera,
                    description = buildAnnotatedString {
                        append("Postar até ")
                        withStyle(style = SpanStyle(color = LioraPink, fontWeight = FontWeight.Bold)) {
                            append("3 storys")
                        }
                        append(" por dia")
                    }
                )
                Spacer(modifier = Modifier.height(16.dp))
                FeatureItem(
                    icon = Icons.Default.Person,
                    description = buildAnnotatedString {
                        append("Visualize até ")
                        withStyle(style = SpanStyle(color = LioraPink, fontWeight = FontWeight.Bold)) {
                            append("100 perfis")
                        }
                        append(" por dia")
                    }
                )
                Spacer(modifier = Modifier.height(16.dp))
                FeatureItem(
                    icon = Icons.Default.Refresh,
                    description = buildAnnotatedString {
                        append("Retornar ao perfil anterior ")
                        withStyle(style = SpanStyle(color = LioraPink, fontWeight = FontWeight.Bold)) {
                            append("ilimitado")
                        }
                    }
                )
                Spacer(modifier = Modifier.height(16.dp))
                FeatureItem(
                    icon = Icons.Default.Favorite,
                    description = buildAnnotatedString { append("Ver os usuários que te curtiram") }
                )
                Spacer(modifier = Modifier.height(16.dp))
                FeatureItem(
                    icon = Icons.Default.Chat,
                    description = buildAnnotatedString {
                        append("Mandar até ")
                        withStyle(style = SpanStyle(color = LioraPink, fontWeight = FontWeight.Bold)) {
                            append("14 mensagem")
                        }
                        append(" adiantadas")
                    }
                )
            }
            Spacer(modifier = Modifier.height(32.dp))
            AssinarButton()
        }
    }
}

@Composable
fun PriceText(price: String) {
    Row(
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.Center
    ) {
        Text(
            text = price,
            color = LioraPink,
            fontSize = 60.sp,
            fontWeight = FontWeight.Bold,
            lineHeight = 60.sp
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(
                text = "R$",
                color = LioraLightText,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = "MÊS",
                color = LioraLightText,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Composable
fun FeatureItem(icon: ImageVector, description: androidx.compose.ui.text.AnnotatedString) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = LioraLightText,
            modifier = Modifier.size(28.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = description,
            color = LioraGrayText,
            fontSize = 16.sp,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun AssinarButton() {
    Button(
        onClick = { /* TODO: Adicionar lógica de assinatura */ },
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp),
        shape = RoundedCornerShape(50),
        colors = ButtonDefaults.buttonColors(
            containerColor = LioraPink
        )
    ) {
        Text(
            text = "Assinar",
            color = LioraLightText,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
fun AssinanteScreenPreview() {
    MaterialTheme {
        AssinanteScreen()
    }
}