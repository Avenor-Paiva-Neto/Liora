package com.example.liora.ui.screens

import android.app.Activity
import android.os.Build
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

// --- Composable Principal: SplashScreen ---
/**
 * Tela de Splash inicial do aplicativo com animação de frases.
 *
 * @param appEntryState O estado inicial do aplicativo (autenticação e perfil)
 * determinado pelo AppEntryViewModel.
 * @param onAnimationComplete Um callback que é invocado quando a animação das frases
 * é concluída E o estado do AppEntryViewModel não é mais 'Loading'.
 */
@Composable
fun SplashScreen(appEntryState: AppEntryUiState, onAnimationComplete: (AppEntryUiState) -> Unit) {
    // Garante que a tela de splash ocupe a tela inteira, escondendo as barras do sistema.
    MakeFullscreen()

    // Frases a serem exibidas na tela de splash.
    val phrases = remember {
        listOf(
            "A beleza aqui é opaca.",
            "A mente, transparente.",
            "Escolha com precisão."
        )
    }

    // Estado mutável para controlar qual frase está sendo destacada no momento.
    var currentPhraseIndex by remember { mutableIntStateOf(0) }
    // NOVO: Estado para rastrear se a animação visual das frases terminou.
    var isVisualAnimationFinished by remember { mutableStateOf(false) }

    // LaunchedEffect para controlar a sequência de animação e o tempo de exibição das frases.
    LaunchedEffect(true) {
        // Tempos ajustados para uma animação mais rápida, mas ainda visível
        val totalDisplayTimePerPhrase = 1500L // Tempo total para cada frase (reduzido de 2000ms)
        val fadeDuration = 300L // Duração da animação de fade (entrada e saída)

        // Itera sobre as frases para animá-las sequencialmente.
        phrases.forEachIndexed { index, _ ->
            currentPhraseIndex = index // Define a frase atual para ser destacada
            delay(totalDisplayTimePerPhrase - fadeDuration) // Espera o tempo de exibição da frase
            if (index < phrases.lastIndex) {
                delay(fadeDuration) // Adiciona um delay extra para o fade-out/fade-in entre frases.
            }
        }
        delay(500) // Pequeno delay final após a última frase.
        isVisualAnimationFinished = true // Marca que a animação visual terminou.
    }

    // NOVO LaunchedEffect: Este espera que AMBAS as condições sejam verdadeiras.
    // 1. A animação visual da SplashScreen terminou (isVisualAnimationFinished = true).
    // 2. O AppEntryViewModel terminou de carregar (appEntryState não é mais Loading).
    LaunchedEffect(isVisualAnimationFinished, appEntryState) {
        if (isVisualAnimationFinished && appEntryState !is AppEntryUiState.Loading) {
            // Se ambas as condições forem atendidas, invoca o callback para navegar.
            onAnimationComplete(appEntryState)
        }
    }

    // Layout da tela de splash: fundo preto e frases centralizadas.
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black), // Fundo preto
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
        ) {
            // Exibe cada frase com animações de tamanho e cor.
            phrases.forEachIndexed { index, phrase ->
                val isHighlighted = index == currentPhraseIndex // Verifica se a frase atual deve ser destacada

                // Animação do tamanho da fonte: maior se destacada, menor caso contrário.
                val animatedFontSize by animateFloatAsState(
                    targetValue = if (isHighlighted) 34.sp.value else 22.sp.value,
                    animationSpec = tween(durationMillis = 300), // Animação de 300ms
                    label = "fontSizeAnim"
                )
                // Animação da cor do texto: branco total se destacada, com transparência caso contrário.
                val animatedTextColor by animateColorAsState(
                    targetValue = if (isHighlighted) Color.White else Color.White.copy(alpha = 0.3f),
                    animationSpec = tween(durationMillis = 300), // Animação de 300ms
                    label = "textColorAnim"
                )

                Text(
                    text = phrase,
                    fontSize = animatedFontSize.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = if (isHighlighted) FontWeight.Black else FontWeight.Bold,
                    color = animatedTextColor,
                    style = TextStyle(letterSpacing = 1.sp),
                    // Anima o tamanho do conteúdo (útil se o texto mudar de tamanho e afetar o layout)
                    modifier = Modifier.animateContentSize(animationSpec = tween(durationMillis = 300))
                )
                Spacer(modifier = Modifier.height(18.dp)) // Espaçamento entre as frases
            }
        }
    }
}

// --- Função MakeFullscreen (Mantida a mesma, garante tela cheia) ---
@Composable
fun MakeFullscreen() {
    val context = LocalContext.current
    val view = LocalView.current

    DisposableEffect(view) {
        val window = (context as? Activity)?.window ?: return@DisposableEffect onDispose {}

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(false)
            window.insetsController?.let {
                it.hide(WindowInsets.Type.systemBars())
                it.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        } else {
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    or View.SYSTEM_UI_FLAG_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION)
        }

        onDispose {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                window.setDecorFitsSystemWindows(true)
                window.insetsController?.show(WindowInsets.Type.systemBars())
            } else {
                @Suppress("DEPRECATION")
                window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE
            }
        }
    }
}
