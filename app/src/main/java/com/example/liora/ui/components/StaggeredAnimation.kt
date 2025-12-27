package com.example.liora.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

/**
 * Um modificador customizado que aplica uma animação de entrada escalonada.
 * O item aparece com um efeito de fade-in e desliza de baixo para cima.
 *
 * @param index O índice do item na lista, usado para calcular o atraso.
 * @param durationMillis A duração da animação para cada item.
 * @param delayPerItemMillis O atraso entre o início da animação de itens consecutivos.
 */
fun Modifier.staggeredEnterAnimation(
    index: Int,
    durationMillis: Int = 500,
    delayPerItemMillis: Int = 100
): Modifier = composed {
    // Estado para controlar se o item deve estar visível ou não
    var isVisible by remember { mutableStateOf(false) }

    // Efeito que roda apenas uma vez quando o item entra na composição
    LaunchedEffect(Unit) {
        // Calcula o atraso total baseado no índice do item
        delay(index.toLong() * delayPerItemMillis)
        // Após o atraso, marca o item como visível para iniciar a animação
        isVisible = true
    }

    // Animação para o valor alpha (transparência)
    val alpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(durationMillis = durationMillis),
        label = "AlphaAnimation"
    )

    // Animação para o deslocamento vertical (efeito de slide-up)
    val translationY by animateDpAsState(
        targetValue = if (isVisible) 0.dp else 30.dp, // Começa 30.dp abaixo e move para 0.dp
        animationSpec = tween(durationMillis = durationMillis),
        label = "TranslationYAnimation"
    )

    // Aplica as transformações gráficas ao Composable
    this.graphicsLayer {
        this.alpha = alpha
        this.translationY = translationY.toPx()
    }
}