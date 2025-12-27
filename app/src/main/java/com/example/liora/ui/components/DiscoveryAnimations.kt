package com.example.liora.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.SpringSpec
import androidx.compose.animation.core.Transition
import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.animation.core.spring
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableStateOf // <--- IMPORT CORRIGIDO AQUI!
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp // <--- IMPORT CORRIGIDO AQUI!
import kotlin.random.Random

// 1. Estados da Animação
// Adicionamos um novo estado 'Dragging' para representar o arraste manual
enum class EjectionState {
    Visible,
    Dragging, // Novo estado para quando o usuário está arrastando
    Ejected
}

// 2. Propriedades que cada elemento animado terá (mantidas)
data class ElementAnimationState(
    val offsetY: Dp,
    val offsetX: Dp,
    val rotation: Float,
    val alpha: Float
)

// 3. O "cérebro" da transição (mantido)
@Composable
fun rememberEjectionTransition(ejectionState: EjectionState): Transition<EjectionState> {
    return updateTransition(targetState = ejectionState, label = "Ejection Transition")
}

// 4. Receitas de animação
// Usaremos spring para o efeito de mola ao soltar
private val EjectionFloatSpring: SpringSpec<Float> = spring(
    dampingRatio = Spring.DampingRatioMediumBouncy,
    stiffness = Spring.StiffnessLow
)
private val EjectionDpSpring: SpringSpec<Dp> = spring(
    dampingRatio = Spring.DampingRatioMediumBouncy,
    stiffness = Spring.StiffnessLow
)

// tween existente para alphas e algumas transições
private val EjectionTween: FiniteAnimationSpec<Float> = tween(durationMillis = 800, easing = FastOutSlowInEasing)


/**
 * Define como um elemento se comporta ao ser ejetado ou arrastado.
 *
 * @param isMainCard Indica se é o cartão principal (que pode ser arrastado manualmente).
 * @param manualOffset O offset manual aplicado durante o arraste pelo usuário.
 * Este offset terá precedência quando o estado for EjectionState.Dragging.
 * @param exitDirection A direção em que o cartão deve sair (positivo para baixo, negativo para cima).
 * Usado para o decay com mola.
 */
@Composable
fun Transition<EjectionState>.animateElementEjection(
    isMainCard: Boolean = false,
    // CORREÇÃO: Usando mutableStateOf diretamente no remember para o tipo State<Offset> e State<Float>
    manualOffset: State<Offset> = remember { mutableStateOf(Offset.Zero) },
    exitDirection: State<Float> = remember { mutableStateOf(0f) }
): State<ElementAnimationState> {
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp
    val screenWidth = configuration.screenWidthDp.dp

    // Random targets para elementos secundários (bio, etc.)
    val randomTargetX = remember {
        if (isMainCard) 0.dp else Random.nextInt(-screenWidth.value.toInt() / 2, screenWidth.value.toInt() / 2).dp
    }
    val randomTargetRotation = remember {
        if (isMainCard) -15f else Random.nextInt(-45, 45).toFloat()
    }

    // Animação do Offset Y
    val offsetY = animateDp(
        transitionSpec = {
            when {
                // Do Visível para Ejetado ou de Arrastando para Ejetado, usa spring para o decay
                EjectionState.Visible isTransitioningTo EjectionState.Ejected -> EjectionDpSpring
                EjectionState.Dragging isTransitioningTo EjectionState.Ejected -> EjectionDpSpring
                else -> tween(600) // Outras transições usam tween
            }
        }, label = "Offset Y"
    ) { state ->
        when (state) {
            EjectionState.Visible -> 0.dp
            EjectionState.Dragging -> {
                // Se estiver arrastando, o offset é o manualOffset
                if (isMainCard) manualOffset.value.y.dp else 0.dp // CORREÇÃO: Usando .dp em Float para Dp
            }
            EjectionState.Ejected -> {
                // Se ejetado, usa a direção de saída para determinar o valor final
                if (exitDirection.value < 0) -screenHeight // Sai para cima
                else screenHeight // Sai para baixo (poderia ser usado para "desfazer" ou outro gesto)
            }
        }
    }

    // Animação do Offset X
    val offsetX = animateDp(
        transitionSpec = {
            when {
                EjectionState.Visible isTransitioningTo EjectionState.Ejected -> EjectionDpSpring
                EjectionState.Dragging isTransitioningTo EjectionState.Ejected -> EjectionDpSpring
                else -> EjectionDpSpring // Mantém spring para outras transições
            }
        }, label = "Offset X"
    ) { state ->
        when (state) {
            EjectionState.Visible -> 0.dp
            EjectionState.Dragging -> {
                // Se estiver arrastando, o offset é o manualOffset
                if (isMainCard) manualOffset.value.x.dp else 0.dp // CORREÇÃO: Usando .dp em Float para Dp
            }
            EjectionState.Ejected -> {
                // Para a main card, não queremos offset X aleatório na saída de swipe para cima
                if (isMainCard && exitDirection.value < 0) 0.dp
                else randomTargetX // Para outros elementos ou saída para outras direções
            }
        }
    }

    // Animação da Rotação
    val rotation = animateFloat(
        transitionSpec = {
            when {
                EjectionState.Visible isTransitioningTo EjectionState.Ejected -> EjectionFloatSpring
                EjectionState.Dragging isTransitioningTo EjectionState.Ejected -> EjectionFloatSpring
                else -> EjectionTween
            }
        }, label = "Rotation"
    ) { state ->
        when (state) {
            EjectionState.Visible -> 0f
            EjectionState.Dragging -> {
                // Pequena rotação baseada no offset X ao arrastar a main card
                // CORREÇÃO: Normalizando a rotação pelo screenWidth.value para evitar valores muito grandes
                if (isMainCard) manualOffset.value.x / screenWidth.value * randomTargetRotation else 0f
            }
            EjectionState.Ejected -> randomTargetRotation
        }
    }

    // Animação do Alpha
    val alpha = animateFloat(
        transitionSpec = {
            when {
                EjectionState.Visible isTransitioningTo EjectionState.Ejected -> tween(600)
                EjectionState.Dragging isTransitioningTo EjectionState.Ejected -> tween(600)
                else -> tween(800)
            }
        }, label = "Alpha"
    ) { state ->
        if (state == EjectionState.Visible || state == EjectionState.Dragging) 1f else 0f
    }

    return remember(this) {
        derivedStateOf {
            ElementAnimationState(
                offsetY = offsetY.value,
                offsetX = offsetX.value,
                rotation = rotation.value,
                alpha = alpha.value
            )
        }
    }
}


/**
 * Função para animar a entrada de elementos, com foco em delays e efeitos escalonados.
 * Pode ser usada para os componentes de um novo perfil.
 * @param delayMillis O atraso antes do início da animação.
 * @param durationMillis A duração da animação.
 */
@Composable
fun animateElementEntry(
    delayMillis: Int = 0,
    durationMillis: Int = 500,
    initialAlpha: Float = 0f,
    targetAlpha: Float = 1f,
    initialOffsetY: Float = 50f,
    targetOffsetY: Float = 0f,
    initialScale: Float = 0.8f,
    targetScale: Float = 1f
): State<ElementAnimationState> {
    // CORREÇÃO: Usando remember para garantir que a animação seja acionada apenas uma vez
    // E não infiniteRepeatable, pois é uma animação de entrada, não um loop
    val entryTransition = updateTransition(targetState = true, label = "Element Entry Transition")

    val alpha = entryTransition.animateFloat(
        transitionSpec = { tween(durationMillis = durationMillis, delayMillis = delayMillis, easing = FastOutSlowInEasing) },
        label = "Entry Alpha"
    ) { isVisible ->
        if (isVisible) targetAlpha else initialAlpha
    }

    val offsetY = entryTransition.animateFloat(
        transitionSpec = { tween(durationMillis = durationMillis, delayMillis = delayMillis, easing = FastOutSlowInEasing) },
        label = "Entry OffsetY"
    ) { isVisible ->
        if (isVisible) targetOffsetY else initialOffsetY
    }

    val scale = entryTransition.animateFloat(
        transitionSpec = { tween(durationMillis = durationMillis, delayMillis = delayMillis, easing = FastOutSlowInEasing) },
        label = "Entry Scale"
    ) { isVisible ->
        if (isVisible) targetScale else initialScale
    }

    return remember {
        derivedStateOf {
            ElementAnimationState(
                offsetY = offsetY.value.dp,
                offsetX = 0.dp, // Não animamos o X na entrada padrão
                rotation = 0f, // Não animamos rotação na entrada padrão
                alpha = alpha.value
            )
        }
    }
}