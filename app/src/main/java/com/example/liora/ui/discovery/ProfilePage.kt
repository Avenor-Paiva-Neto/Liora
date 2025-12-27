package com.example.liora.ui.discovery

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.example.liora.ui.conversations.ConversationListItem
import com.example.liora.ui.discovery.components.BioSection
import com.example.liora.ui.discovery.components.ProfileHeader
import kotlinx.coroutines.launch

@Composable
fun ProfilePage(
    conversationListItem: ConversationListItem,
    onEjected: (DiscoveryAction) -> Unit,
    onAction: (DiscoveryAction) -> Unit,
    modifier: Modifier = Modifier
) {
    val userProfile = conversationListItem.userProfile
    var isBioExpanded by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    val dragOffset = remember { Animatable(Offset.Zero, Offset.VectorConverter) }
    val extraScale = remember { Animatable(1f) }

    val headerEntryAnimatable = remember { Animatable(0f) }
    val bioEntryAnimatable = remember { Animatable(0f) }

    LaunchedEffect(key1 = userProfile.uid) {
        dragOffset.snapTo(Offset.Zero)
        extraScale.snapTo(1f)

        launch {
            headerEntryAnimatable.animateTo(1f, animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessMedium))
        }
        launch {
            bioEntryAnimatable.animateTo(1f, animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessMedium))
        }
    }

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val screenWidth = this.maxWidth
        val density = LocalDensity.current

        fun triggerSwipeEjection() {
            coroutineScope.launch {
                val targetOffsetX = if (dragOffset.value.x > 0) with(density) { screenWidth.toPx() } * 1.5f else with(density) { -screenWidth.toPx() } * 1.5f

                dragOffset.animateTo(
                    targetValue = Offset(targetOffsetX, dragOffset.value.y),
                    animationSpec = spring(dampingRatio = 0.8f, stiffness = 350f)
                )
                onEjected(DiscoveryAction.Skip)
            }
        }

        fun triggerLikeAnimation() {
            coroutineScope.launch {
                extraScale.animateTo(
                    targetValue = 0.5f,
                    animationSpec = tween(durationMillis = 300)
                )
                onEjected(DiscoveryAction.Like)
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(userProfile.uid) {
                    detectDragGestures(
                        onDrag = { change, dragAmount ->
                            change.consume()
                            coroutineScope.launch {
                                dragOffset.snapTo(dragOffset.value + dragAmount)
                            }
                        },
                        onDragEnd = {
                            coroutineScope.launch {
                                val dragThreshold = with(density) { screenWidth.toPx() * 0.3f }
                                val currentOffsetX = dragOffset.value.x

                                if (currentOffsetX > dragThreshold || currentOffsetX < -dragThreshold) {
                                    triggerSwipeEjection()
                                } else {
                                    dragOffset.animateTo(Offset.Zero, animationSpec = spring(dampingRatio = 0.6f, stiffness = 150f))
                                }
                            }
                        }
                    )
                }
                .graphicsLayer {
                    translationX = dragOffset.value.x
                    translationY = dragOffset.value.y
                    rotationZ = (dragOffset.value.x / with(density) { screenWidth.toPx() }) * 20f
                    scaleX = extraScale.value
                    scaleY = extraScale.value
                    alpha = extraScale.value
                }
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .graphicsLayer {
                        alpha = headerEntryAnimatable.value
                        translationY = (1f - headerEntryAnimatable.value) * 100f
                    }
            ) {
                ProfileHeader(
                    conversationListItem = conversationListItem,
                    screenWidth = screenWidth,
                    onAction = { action ->
                        // **BLOCO CORRIGIDO AQUI**
                        // Simplificamos para tratar os casos de animação e passar o resto adiante.
                        when (action) {
                            is DiscoveryAction.Like -> triggerLikeAnimation()
                            is DiscoveryAction.Chat, is DiscoveryAction.Skip -> triggerSwipeEjection()
                            // Todas as outras ações (Undo, Report, BioToggle, Retry, etc.)
                            // são passadas diretamente para o ViewModel.
                            else -> onAction(action)
                        }
                    }
                )
            }
            Spacer(Modifier.height(16.dp))
            Box(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .graphicsLayer {
                        alpha = bioEntryAnimatable.value
                        translationX = (1f - bioEntryAnimatable.value) * -100f
                    }
            ) {
                BioSection(
                    bio = userProfile.bio,
                    isExpanded = isBioExpanded,
                    onToggle = { isBioExpanded = !isBioExpanded }
                )
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}