package com.example.liora.ui.conversations

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.liora.domain.usecase.DetermineImageBlurStatusUseCase // Importe o novo Use Case
import com.example.liora.domain.usecase.ObserveMatchedUsersUseCase
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map // Importe o operador map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch

class ConversationsViewModel(
    application: Application,
    private val observeMatchedUsersUseCase: ObserveMatchedUsersUseCase,
    private val determineImageBlurStatusUseCase: DetermineImageBlurStatusUseCase, // NOVO: Injeção do Use Case de blur
    private val firebaseAuth: FirebaseAuth
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow<ConversationsUiState>(ConversationsUiState.Loading)
    val uiState: StateFlow<ConversationsUiState> = _uiState.asStateFlow()

    init {
        loadConversations() // Renomeado para melhor clareza, reflete o conteúdo do UI State
    }

    private fun loadConversations() { // Lógica principal para carregar as conversas com info de blur
        viewModelScope.launch {
            val currentUserId = firebaseAuth.currentUser?.uid
            if (currentUserId == null) {
                _uiState.value = ConversationsUiState.Error("Usuário não autenticado.")
                return@launch
            }

            // O observeMatchedUsersUseCase agora emite uma lista de Pair<UserProfile, String (chatId)>
            observeMatchedUsersUseCase(currentUserId)
                .onStart { _uiState.value = ConversationsUiState.Loading }
                .catch { e -> _uiState.value = ConversationsUiState.Error(e.message ?: "Erro ao carregar conversas.") }
                .map { matchedUsersWithChatIds ->
                    // Mapeia a lista de Pair<UserProfile, ChatId> para uma lista de ConversationListItem.
                    // Para cada par, o DetermineImageBlurStatusUseCase é invocado para obter
                    // as informações de exibição da imagem (incluindo se deve ser borrada).
                    matchedUsersWithChatIds.map { (userProfile, chatId) ->
                        val imageDisplayInfo = determineImageBlurStatusUseCase(userProfile, chatId)
                        ConversationListItem(userProfile, imageDisplayInfo)
                    }
                }
                .collect { conversationListItems ->
                    // Coleta a lista final de ConversationListItem e atualiza o UI State.
                    _uiState.value = ConversationsUiState.Success(conversationListItems)
                }
        }
    }
}