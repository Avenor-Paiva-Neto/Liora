package com.example.liora.ui.components

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth // <--- NOVA IMPORTAÇÃO
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp

import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.liora.ui.AppRoutes
import com.example.liora.ui.theme.AccentPrimary
import com.example.liora.ui.theme.TextSecondary
import kotlinx.coroutines.delay
import kotlin.math.abs


// **NOVAS IMPORTAÇÕES NECESSÁRIAS**
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.material3.Surface
import com.example.liora.ui.theme.*


/**
 * Define os itens de navegação da barra inferior.
 */
sealed class BottomNavItem(
    val route: String,
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    object Home : BottomNavItem(AppRoutes.DISCOVERY, "Início", Icons.Filled.Home, Icons.Outlined.Home)
    object Chat : BottomNavItem(AppRoutes.CHAT_LIST, "Chat", Icons.Filled.ChatBubble, Icons.Outlined.ChatBubbleOutline)
    object Likes : BottomNavItem(AppRoutes.LIKES, "Curtidas", Icons.Filled.Favorite, Icons.Outlined.FavoriteBorder)
    object Profile : BottomNavItem(AppRoutes.PERFIL, "Perfil", Icons.Filled.Person, Icons.Outlined.Person)
}

/**
 * A barra de navegação inferior para o aplicativo Liora, com animações avançadas de física:
 * - Ícone selecionado "salta" alto, "colide" no topo e "cai" com rotação e rebote.
 * - Uma "onda" de movimento se propaga para os ícones vizinhos, fazendo-os "flutuar" e inclinar.
 * - **Estilo flutuante e arredondado, com sombra e fundo transparente.**
 *
 * @param navController O NavController que gerencia a navegação do aplicativo.
 * @param currentRoute A string da rota atualmente selecionada, para destacar o item correto.
 * @param modifier O modificador a ser aplicado ao componente.
 */
@Composable
fun LioraBottomNavBar(
    navController: NavController,
    currentRoute: String?,
    modifier: Modifier = Modifier
) {
    val items = listOf(
        BottomNavItem.Home,
        BottomNavItem.Chat,
        BottomNavItem.Likes,
        BottomNavItem.Profile
    )

    val context = LocalContext.current
    val vibrator = remember { context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator? }

    var waveTrigger by remember { mutableStateOf(-1) }
    var lastSelectedItemIndex by remember { mutableStateOf(-1) }

    val selectedItemIndex = items.indexOfFirst { it.route == currentRoute }

    LaunchedEffect(currentRoute) {
        val newSelectedIndex = items.indexOfFirst { it.route == currentRoute }
        if (newSelectedIndex != -1 && newSelectedIndex != lastSelectedItemIndex) {
            waveTrigger = newSelectedIndex
            lastSelectedItemIndex = newSelectedIndex
            delay(150)
            waveTrigger = -1
        }
    }

    // Usamos um Box para conter a NavigationBar e aplicar o padding e sombra/clip a ela.
    // Isso garante que a área do NavigationBarItemDefaults.colors(indicatorColor)
    // seja transparente por padrão, e que o padding externo do Box crie o espaço.
    Box(
        modifier = modifier
            .fillMaxWidth() // Ocupa a largura total para que o padding funcione
            .padding(horizontal = 16.dp, vertical = 8.dp) // O padding externo para a sensação flutuante
    ) {
        NavigationBar(
            modifier = Modifier
                .height(80.dp)
                .shadow(
                    elevation = 16.dp,
                    shape = RoundedCornerShape(24.dp)
                )
                .clip(RoundedCornerShape(24.dp)), // Arredonda os cantos da barra interna
            containerColor = BackgroundPrimary, // Cor de fundo da barra em si
            tonalElevation = 0.dp // Garantimos que não haja sombra padrão
        ) {
            items.forEachIndexed { index, item ->
                val isSelected = currentRoute == item.route

                val selectedIconJumpY by animateDpAsState(
                    targetValue = if (isSelected) { (-20).dp } else { 0.dp },
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow,
                        visibilityThreshold = 0.5.dp
                    ),
                    label = "selectedIconJumpY"
                )

                val selectedIconRotation by animateFloatAsState(
                    targetValue = if (isSelected) 360f else 0f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMedium
                    ),
                    label = "selectedIconRotation"
                )

                val distance = (index - selectedItemIndex).toFloat()
                val absDistance = abs(distance)

                val waveIntensity = remember(absDistance) {
                    when {
                        absDistance == 0f -> if (isSelected) 1.0f else 0.0f
                        absDistance > 0f && absDistance <= 1f -> 0.7f
                        absDistance > 1f && absDistance <= 2f -> 0.3f
                        else -> 0.0f
                    }
                }

                val waveOffsetAnim by animateDpAsState(
                    targetValue = if (waveTrigger == index) {
                        if (isSelected) (-8).dp else (-4).dp * waveIntensity
                    } else if (absDistance > 0f && waveTrigger != -1) {
                        val propagationEffect = if (absDistance <= 1f) 1.0f else 0.5f
                        ((-4).dp * waveIntensity * propagationEffect)
                    } else {
                        0.dp
                    },
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow,
                        visibilityThreshold = 0.5.dp
                    ),
                    label = "waveOffsetAnim"
                )

                val waveRotationAnim by animateFloatAsState(
                    targetValue = if (waveTrigger == index) {
                        if (isSelected) 0f else (distance * 5f * waveIntensity)
                    } else if (absDistance > 0f && waveTrigger != -1) {
                        if (distance > 0) (-5f * waveIntensity) else (5f * waveIntensity)
                    } else {
                        0f
                    },
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioLowBouncy,
                        stiffness = Spring.StiffnessLow,
                        visibilityThreshold = 0.5f
                    ),
                    label = "waveRotationAnim"
                )

                val iconTint by animateColorAsState(
                    targetValue = if (isSelected) AccentPrimary else TextSecondary,
                    animationSpec = tween(durationMillis = 200),
                    label = "iconColor"
                )

                NavigationBarItem(
                    selected = isSelected,
                    onClick = {
                        navController.navigate(item.route) {
                            launchSingleTop = true
                            restoreState = true
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                        }
                        vibrator?.let {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                it.vibrate(VibrationEffect.createOneShot(70, VibrationEffect.DEFAULT_AMPLITUDE))
                            } else {
                                @Suppress("DEPRECATION")
                                it.vibrate(70)
                            }
                        }
                    },
                    icon = {
                        Box(
                            modifier = Modifier
                                .size(60.dp)
                                .offset(
                                    y = if (isSelected) selectedIconJumpY else waveOffsetAnim
                                )
                                .graphicsLayer {
                                    rotationZ = if (isSelected) selectedIconRotation else waveRotationAnim
                                    transformOrigin = TransformOrigin(0.5f, 1.0f)
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                                contentDescription = item.title,
                                modifier = Modifier.size(28.dp),
                                tint = iconTint
                            )
                        }
                    },
                    label = null,
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = AccentPrimary,
                        unselectedIconColor = TextSecondary,
                        // MUITO IMPORTANTE: TORNAR O INDICATOR COLOR TRANSPARENTE
                        indicatorColor = Color.Transparent, // Garante que a área do item NÃO tenha cor de fundo
                        selectedTextColor = Color.Transparent,
                        unselectedTextColor = Color.Transparent
                    )
                )
            }
        }
    }
}