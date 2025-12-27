package com.example.liora.ui.chat

import android.app.Application
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.liora.di.AppViewModelFactory
import com.example.liora.domain.model.Message
import com.example.liora.ui.theme.LioraTheme
import java.text.SimpleDateFormat
import java.util.*

// Cores extraídas da imagem para replicar o design
private val ScreenBackground = Color.Black
private val PurpleUi = Color(0xFF6243A8)
private val DarkGrayBubble = Color(0xFF2E2E2E)
private val TextColorLight = Color.White
private val TextColorHint = Color.LightGray

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    navController: NavController,
    matchedUserId: String
) {
    val application = LocalContext.current.applicationContext as Application

    val viewModel: ChatViewModel = viewModel(
        factory = AppViewModelFactory(application)
    )

    val uiState by viewModel.uiState.collectAsState()
    val messageInput by viewModel.messageInput.collectAsState()

    LaunchedEffect(matchedUserId) {
        // Lógica da ViewModel permanece a mesma
    }

    Scaffold(
        containerColor = ScreenBackground,
        topBar = {
            TopAppBar(
                title = { /* Título vazio, conforme o design */ },
                navigationIcon = {
                    Row(
                        modifier = Modifier
                            .clickable { navController.popBackStack() }
                            .padding(horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "Voltar",
                            tint = TextColorLight
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Fechar",
                            color = TextColorLight,
                            fontSize = 18.sp
                        )
                    }
                },
                actions = {
                    // Lógica do status (Online/Offline/Digitando)
                    if (uiState is ChatUiState.Success) {
                        val state = uiState as ChatUiState.Success

                        // Determina o texto e a cor da pílula baseando-se no status
                        val (statusText, statusColor) = when {
                            state.isMatchedUserTyping -> "DIGITANDO..." to PurpleUi
                            state.isMatchedUserOnline -> "ONLINE" to PurpleUi
                            else -> "OFFLINE" to DarkGrayBubble // Cinza quando offline
                        }

                        Button(
                            onClick = { /* Nenhuma ação especificada */ },
                            modifier = Modifier.padding(end = 8.dp),
                            shape = RoundedCornerShape(50),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = statusColor // Cor dinâmica
                            ),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Text(statusText, color = TextColorLight, style = MaterialTheme.typography.labelMedium)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = messageInput,
                    onValueChange = { viewModel.onMessageInputChanged(it) },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Digite sua mensagem...", color = TextColorHint) },
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        cursorColor = PurpleUi,
                        focusedTextColor = TextColorLight,
                        unfocusedTextColor = TextColorLight,
                        focusedContainerColor = DarkGrayBubble,
                        unfocusedContainerColor = DarkGrayBubble,
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent
                    )
                )
                Spacer(modifier = Modifier.width(8.dp))
                FloatingActionButton(
                    onClick = { viewModel.sendMessage() },
                    shape = CircleShape,
                    containerColor = PurpleUi,
                    contentColor = TextColorLight
                ) {
                    Icon(Icons.Filled.Send, contentDescription = "Enviar Mensagem")
                }
            }
        }
    ) { paddingValues ->
        when (val state = uiState) {
            is ChatUiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = PurpleUi)
                }
            }
            is ChatUiState.Error -> {
                Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                    Text("Erro: ${state.message}", color = MaterialTheme.colorScheme.error, textAlign = TextAlign.Center)
                }
            }
            is ChatUiState.Success -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(horizontal = 8.dp),
                    reverseLayout = true,
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    items(state.messages.reversed()) { message ->
                        MessageBubble(message = message, currentUserId = viewModel.currentUserId ?: "")
                    }
                }
            }
        }
    }
}

@Composable
fun MessageBubble(message: Message, currentUserId: String) {
    val isCurrentUser = message.senderId == currentUserId
    val alignment = if (isCurrentUser) Alignment.End else Alignment.Start

    val bubbleColor = if (isCurrentUser) PurpleUi else DarkGrayBubble
    val textColor = TextColorLight

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalAlignment = alignment
    ) {
        Surface(
            color = bubbleColor,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.widthIn(max = 300.dp)
        ) {
            Column(modifier = Modifier.padding(10.dp)) {
                Text(
                    text = message.content,
                    color = textColor,
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(message.timestamp)),
                        color = textColor.copy(alpha = 0.7f),
                        fontSize = 10.sp
                    )
                    if (isCurrentUser) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = message.status,
                            color = textColor.copy(alpha = 0.7f),
                            fontSize = 10.sp
                        )
                    }
                }
            }
        }
    }
}


@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
fun ChatScreenPreview() {
    LioraTheme {
        ChatScreen(navController = rememberNavController(), matchedUserId = "test_user_id_123")
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
fun MessageBubblePreview() {
    LioraTheme {
        Column(Modifier.padding(8.dp)) {
            MessageBubble(
                message = Message(
                    id = "2",
                    senderId = "user2",
                    content = "Oi joia ?",
                    timestamp = System.currentTimeMillis(),
                    type = "text",
                    status = "delivered"
                ),
                currentUserId = "user1"
            )
            Spacer(modifier = Modifier.height(8.dp))
            MessageBubble(
                message = Message(
                    id = "1",
                    senderId = "user1",
                    content = "oi tudo bem, te achei interessante kk",
                    timestamp = System.currentTimeMillis() + 1000,
                    type = "text",
                    status = "sent"
                ),
                currentUserId = "user1"
            )
        }
    }
}