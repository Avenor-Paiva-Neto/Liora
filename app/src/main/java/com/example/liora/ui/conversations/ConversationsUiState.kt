package com.example.liora.ui.conversations

// Importe as classes necessárias
import com.example.liora.domain.model.ImageDisplayInfo
import com.example.liora.domain.model.UserProfile

/**
 * Define os possíveis estados da UI para a tela de Conversas.
 *
 * Esta sealed interface ajuda a representar os diferentes estados (carregando, sucesso, erro)
 * de forma segura e explícita, facilitando o tratamento na UI.
 */
sealed interface ConversationsUiState { // Mantido como sealed interface

    /**
     * Estado de carregamento, indicando que os dados estão sendo buscados.
     */
    data object Loading : ConversationsUiState

    /**
     * Estado de sucesso, contendo a lista de itens de conversa para exibição.
     *
     * **MODIFICADO:** Agora contém uma lista de [ConversationListItem],
     * que inclui tanto o [UserProfile] quanto o [ImageDisplayInfo] para o blur.
     *
     * @property conversations A lista de [ConversationListItem] a ser exibida.
     */
    data class Success(val conversations: List<ConversationListItem>) : ConversationsUiState // ALTERADO AQUI

    /**
     * Estado de erro, indicando que algo deu errado durante o carregamento dos dados.
     *
     * @property message A mensagem de erro a ser exibida ao usuário.
     */
    data class Error(val message: String) : ConversationsUiState
}