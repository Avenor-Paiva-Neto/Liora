package com.example.liora.ui.screens

import android.app.Application
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.liora.di.AppViewModelFactory
import com.example.liora.ui.AppRoutes
import com.example.liora.ui.discovery.DiscoveryAction
import com.example.liora.ui.discovery.DiscoveryEvent
import com.example.liora.ui.discovery.DiscoveryUiState
import com.example.liora.ui.discovery.DiscoveryViewModel
import com.example.liora.ui.discovery.ProfilePage
import com.example.liora.ui.discovery.components.MatchDialog
import com.example.liora.ui.discovery.components.NoProfilesFoundScreen
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiscoveryScreen(
    navController: NavController
) {
    val application = LocalContext.current.applicationContext as Application
    val viewModel: DiscoveryViewModel = viewModel(factory = AppViewModelFactory(application))
    val uiState by viewModel.uiState.collectAsState()
    val matchState by viewModel.matchState.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(key1 = true) {
        viewModel.eventFlow.collectLatest { event ->
            when (event) {
                is DiscoveryEvent.ShowToast -> {
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar(
                            message = event.message,
                            duration = SnackbarDuration.Short
                        )
                    }
                }
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        containerColor = Color.Black
    ) { paddingValues ->
        Surface(modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues), color = Color.Black) {
            when (val state = uiState) {
                is DiscoveryUiState.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                is DiscoveryUiState.Error -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text(text = "Erro: ${state.message}", color = Color.Red, textAlign = TextAlign.Center) }
                is DiscoveryUiState.Success -> {
                    if (state.profiles.isEmpty()) {
                        // MUDANÇA PRINCIPAL AQUI
                        // Usando o novo NoProfilesFoundScreen com os callbacks corretos.
                        NoProfilesFoundScreen(
                            onUndoClick = { viewModel.onAction(DiscoveryAction.Undo) },
                            onRetryClick = { viewModel.onAction(DiscoveryAction.Retry) }
                        )
                    } else {
                        // A sua lógica de ProfilePage está estável e foi mantida.
                        // Usando o Box que definimos para exibir um card por vez.
                        val topProfile = state.profiles.first()

                        Box(modifier = Modifier.fillMaxSize()) {
                            ProfilePage(
                                conversationListItem = topProfile,
                                onEjected = { action -> viewModel.onAction(action) },
                                onAction = { action -> viewModel.onAction(action) }
                            )
                        }
                    }

                    matchState?.let { matchedUser ->
                        val currentUserProfile = viewModel.getCurrentUserProfile()
                        if (currentUserProfile != null) {
                            MatchDialog(
                                currentUserProfile = currentUserProfile,
                                matchedUser = matchedUser,
                                onDismissRequest = { viewModel.clearMatchState() },
                                onSendMessageRequest = {
                                    viewModel.clearMatchState()
                                    navController.navigate("${AppRoutes.INDIVIDUAL_CHAT}/${matchedUser.uid}")
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}