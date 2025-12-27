package com.example.liora.ui.screens

import android.app.Application
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.liora.di.AppViewModelFactory
import com.example.liora.domain.model.UserProfile
import com.example.liora.ui.AppRoutes
import com.example.liora.ui.conversations.ConversationListItem
import com.example.liora.ui.discovery.components.MatchDialog
import com.example.liora.ui.likes.LikesUiState
import com.example.liora.ui.likes.LikesViewModel
import com.example.liora.ui.theme.*
import kotlinx.coroutines.delay

private sealed class DismissState {
    object Default : DismissState()
    object DismissingLeft : DismissState()
    object DismissingRight : DismissState()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LikesScreen(
    onNavigateBack: () -> Unit,
    onNavigateToProfile: (String) -> Unit,
    onNavigateToChat: (String) -> Unit
) {
    val application = LocalContext.current.applicationContext as Application
    val viewModel: LikesViewModel = viewModel(factory = AppViewModelFactory(application))
    val uiState by viewModel.uiState.collectAsState()
    val newMatch by viewModel.newMatchState.collectAsState()
    val currentUserProfile by viewModel.currentUserProfile.collectAsState()

    Scaffold(
        topBar = { AppTopBar(title = "CURTIDAS", onBackClick = onNavigateBack) },
        containerColor = BackgroundPrimary
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.padding(paddingValues)) {
                Divider(
                    color = TextSecondary.copy(alpha = 0.3f),
                    thickness = 1.dp,
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
                Text(
                    text = "Todos que te curtiram aparecerão aqui, clique sobre a imagem para ver seu perfil",
                    color = TextSecondary,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(top = 16.dp, start = 20.dp, end = 20.dp)
                )

                when (val state = uiState) {
                    is LikesUiState.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                    is LikesUiState.Error -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text(state.message, color = Color.Red, textAlign = TextAlign.Center) }
                    is LikesUiState.Empty -> EmptyLikesState(modifier = Modifier.weight(1f))
                    is LikesUiState.Success -> {
                        @OptIn(ExperimentalFoundationApi::class)
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(top = 32.dp),
                            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(32.dp)
                        ) {
                            itemsIndexed(items = state.profiles, key = { _, item -> item.userProfile.uid }) { index, conversationListItem ->
                                LikeItem(
                                    conversationListItem = conversationListItem,
                                    index = index,
                                    onDismiss = { viewModel.onDismissClicked(conversationListItem.userProfile) },
                                    onLike = { viewModel.onLikeClicked(conversationListItem.userProfile) },
                                    onViewProfile = { onNavigateToProfile(conversationListItem.userProfile.uid) },
                                    modifier = Modifier.animateItemPlacement(animationSpec = tween(durationMillis = 500))
                                )
                            }
                        }
                    }
                }
            }

            newMatch?.let { matchedUser ->
                currentUserProfile?.let { user ->
                    MatchDialog(
                        currentUserProfile = user,
                        matchedUser = matchedUser,
                        onDismissRequest = { viewModel.clearMatchState() },
                        onSendMessageRequest = {
                            viewModel.clearMatchState()
                            onNavigateToChat(matchedUser.uid)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun LikeItem(
    conversationListItem: ConversationListItem,
    index: Int,
    onDismiss: () -> Unit,
    onLike: () -> Unit,
    onViewProfile: () -> Unit,
    modifier: Modifier = Modifier
) {
    val userProfile = conversationListItem.userProfile
    var dismissState by remember { mutableStateOf<DismissState>(DismissState.Default) }
    val animationSpec = tween<Float>(durationMillis = 400)
    val screenWidthPx = with(LocalDensity.current) { LocalConfiguration.current.screenWidthDp.dp.toPx() }

    val offsetX by animateFloatAsState(
        targetValue = when (dismissState) {
            DismissState.DismissingLeft -> -screenWidthPx
            DismissState.DismissingRight -> screenWidthPx
            else -> 0f
        },
        animationSpec = tween(durationMillis = 400), label = ""
    )
    val rotationZ by animateFloatAsState(targetValue = when (dismissState) {
        DismissState.DismissingLeft -> -15f
        DismissState.DismissingRight -> 15f
        else -> 0f
    }, animationSpec = animationSpec, label = "")
    val alpha by animateFloatAsState(targetValue = if (dismissState == DismissState.Default) 1f else 0f, animationSpec = animationSpec, label = "")

    LaunchedEffect(dismissState) {
        if (dismissState != DismissState.Default) {
            delay(400)
            if (dismissState == DismissState.DismissingLeft) onDismiss() else onLike()
        }
    }

    val isReversed = index % 2 != 0
    Row(
        modifier = modifier
            .height(250.dp)
            .fillMaxWidth()
            .graphicsLayer(
                translationX = offsetX,
                rotationZ = rotationZ,
                alpha = alpha
            ),
        verticalAlignment = Alignment.Top
    ) {
        if (isReversed) {
            ProfileInfoBlock(
                profile = userProfile,
                onDismiss = { dismissState = DismissState.DismissingLeft },
                onLike = { dismissState = DismissState.DismissingRight },
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(16.dp))
            ProfileImageCard(conversationListItem = conversationListItem, onViewProfile = onViewProfile)
        } else {
            ProfileImageCard(conversationListItem = conversationListItem, onViewProfile = onViewProfile)
            Spacer(modifier = Modifier.width(16.dp))
            ProfileInfoBlock(
                profile = userProfile,
                onDismiss = { dismissState = DismissState.DismissingLeft },
                onLike = { dismissState = DismissState.DismissingRight },
                modifier = Modifier.weight(1f)
            )
        }
    }
}


@Composable
private fun ProfileImageCard(
    conversationListItem: ConversationListItem,
    onViewProfile: () -> Unit
) {
    val userProfile = conversationListItem.userProfile
    val imageDisplayInfo = conversationListItem.imageDisplayInfo

    Box(
        modifier = Modifier
            .width(170.dp)
            .height(250.dp)
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onViewProfile),
        contentAlignment = Alignment.Center
    ) {
        AsyncImage(
            model = imageDisplayInfo.imageUrl,
            contentDescription = "Foto de ${userProfile.name}",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .then(
                    if (imageDisplayInfo.shouldBlur) Modifier.blur(12.dp)
                    else Modifier
                )
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.6f)),
                        startY = 300f
                    )
                )
        )
        Text(text = "VISUALIZAR", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
    }
}

@Composable
private fun ProfileInfoBlock(
    profile: UserProfile,
    onDismiss: () -> Unit,
    onLike: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxHeight(),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(
                text = profile.name.uppercase(),
                color = TextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 19.sp,
                letterSpacing = 0.5.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "Te curtiu", color = TextSecondary, fontSize = 14.sp)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            IconButton(
                onClick = onLike,
                modifier = Modifier
                    .size(58.dp)
                    .background(LightButtonBackground, CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Outlined.FavoriteBorder,
                    contentDescription = "Curtir ${profile.name}",
                    tint = LightButtonText
                )
            }
            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .size(58.dp)
                    .border(1.5.dp, TextSecondary, CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Rejeitar ${profile.name}",
                    tint = TextPrimary
                )
            }
        }
    }
}

@Composable
private fun EmptyLikesState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Ninguém ainda te curtiu",
            color = TextSecondary,
            fontSize = 16.sp,
            textAlign = TextAlign.Center
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBar(title: String, onBackClick: () -> Unit) {
    TopAppBar(
        title = { Text(text = title, color = TextPrimary, fontWeight = FontWeight.Bold) },
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(imageVector = Icons.Default.ArrowBackIosNew, contentDescription = "Voltar", tint = TextPrimary)
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = BackgroundPrimary)
    )
}