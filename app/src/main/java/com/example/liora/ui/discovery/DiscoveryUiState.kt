package com.example.liora.ui.discovery

import com.example.liora.ui.conversations.ConversationListItem // Importe ConversationListItem

sealed interface DiscoveryUiState {
    data object Loading : DiscoveryUiState
    // MODIFICADO: Agora Success contém uma lista de ConversationListItem
    data class Success(val profiles: List<ConversationListItem>) : DiscoveryUiState
    data class Error(val message: String) : DiscoveryUiState
}