package com.example.liora.ui.chat

import com.example.liora.domain.model.Message
import com.example.liora.domain.model.UserProfile

/**
 * Sealed interface que representa os diferentes estados da interface do usuário (UI)
 * da tela de chat.
 *
 * No padrão MVVM (Model-View-ViewModel), o ViewModel expõe um StateFlow ou LiveData
 * que representa o estado atual da UI. Uma sealed interface é uma escolha excelente
 * para representar esses estados, pois permite definir um conjunto restrito e conhecido
 * de subclasses, garantindo que o ViewModel só possa emitir estados válidos e que a
 * UI trate todos os casos possíveis de forma exaustiva.
 */
sealed interface ChatUiState {
    /**
     * Representa o estado em que a tela de chat está carregando dados (por exemplo,
     * buscando mensagens iniciais ou o perfil do usuário correspondido).
     */
    data object Loading : ChatUiState

    /**
     * Representa o estado em que os dados foram carregados com sucesso e estão
     * prontos para serem exibidos na UI.
     *
     * @property messages A lista de mensagens da conversa. Esta lista será atualizada
     * em tempo real via Flow.
     * @property matchedUser O perfil completo do usuário com quem o chat está ocorrendo.
     * Essencial para exibir o nome, foto e outras informações na barra superior do chat.
     * @property isMatchedUserOnline Indica se o usuário correspondido está online. Usado
     * para exibir o status de presença.
     * @property isMatchedUserTyping Indica se o usuário correspondido está digitando. Usado
     * para exibir o indicador "digitando...".
     */
    data class Success(
        val messages: List<Message>,
        val matchedUser: UserProfile,
        val isMatchedUserOnline: Boolean,
        val isMatchedUserTyping: Boolean
    ) : ChatUiState

    /**
     * Representa o estado em que ocorreu um erro durante o carregamento ou
     * processamento dos dados.
     *
     * @property message Uma mensagem descritiva do erro, que pode ser exibida ao usuário.
     */
    data class Error(val message: String) : ChatUiState
}
