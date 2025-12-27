package com.example.liora.ui.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.example.liora.domain.model.Message
import com.example.liora.domain.model.UserProfile
import com.example.liora.domain.usecase.ObserveMessagesUseCase
import com.example.liora.domain.usecase.ObservePresenceUseCase
import com.example.liora.domain.usecase.ObserveTypingStatusUseCase
import com.example.liora.domain.usecase.SendMessageUseCase
import com.example.liora.domain.usecase.UpdateMessageStatusUseCase
import com.example.liora.domain.usecase.UpdatePresenceUseCase
import com.example.liora.domain.usecase.UpdateTypingStatusUseCase
import com.example.liora.domain.repository.UserRepository
import com.example.liora.domain.usecase.EnsureChatDocumentUseCase // IMPORTAR AQUI
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.util.UUID

/**
 * ViewModel para a tela de chat.
 * Gerencia o estado da UI ([ChatUiState]), expõe dados para a [ChatScreen]
 * e lida com a lógica de apresentação e eventos do usuário, delegando
 * as operações de negócio aos Use Cases.
 *
 * Esta ViewModel é pura (não [AndroidViewModel]) e recebe [SavedStateHandle]
 * para acessar argumentos de navegação.
 */
class ChatViewModel(
    private val savedStateHandle: SavedStateHandle,
    private val sendMessageUseCase: SendMessageUseCase,
    private val observeMessagesUseCase: ObserveMessagesUseCase,
    private val updateMessageStatusUseCase: UpdateMessageStatusUseCase,
    private val updatePresenceUseCase: UpdatePresenceUseCase,
    private val observePresenceUseCase: ObservePresenceUseCase,
    private val updateTypingStatusUseCase: UpdateTypingStatusUseCase,
    private val observeTypingStatusUseCase: ObserveTypingStatusUseCase,
    private val userRepository: UserRepository,
    private val firebaseAuth: FirebaseAuth,
    private val ensureChatDocumentUseCase: EnsureChatDocumentUseCase // INJETAR AQUI: Nova dependência para o Use Case
) : ViewModel() {

    // Estado da UI, exposto para a ChatScreen observar
    private val _uiState = MutableStateFlow<ChatUiState>(ChatUiState.Loading)
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    // Estado do campo de entrada de mensagem
    private val _messageInput = MutableStateFlow("")
    val messageInput: StateFlow<String> = _messageInput.asStateFlow()

    // Variável agora pública para que a ChatScreen possa acessá-la e passar para MessageBubble
    val currentUserId: String?
    private var matchedUserId: String? = null
    private var matchedUserProfile: UserProfile? = null
    private var matchId: String? = null

    init {
        currentUserId = firebaseAuth.currentUser?.uid
        matchedUserId = savedStateHandle.get<String>("matchedUserId")

        if (currentUserId == null || matchedUserId == null) {
            _uiState.value = ChatUiState.Error("Erro: ID de usuário ou ID do match não encontrado.")
        } else {
            loadChatData()
        }
    }

    /**
     * Carrega os dados iniciais do chat, incluindo o perfil do usuário correspondido.
     * Esta função é chamada uma vez na inicialização do ViewModel.
     *
     * ALTERAÇÃO AQUI: Adição da chamada a ensureChatDocumentUseCase.
     */
    private fun loadChatData() {
        viewModelScope.launch {
            try {
                val userProfileResult = matchedUserId?.let { userRepository.getUserData(it) }

                userProfileResult?.onSuccess { userProfile ->
                    if (userProfile != null) {
                        matchedUserProfile = userProfile
                        matchId = calculateMatchId(currentUserId!!, matchedUserId!!)

                        // GARANTIR QUE O DOCUMENTO DO CHAT EXISTA ANTES DE TENTAR OBSERVAR/ENVIAR MENSAGENS
                        val ensureDocResult = ensureChatDocumentUseCase(
                            matchId!!,
                            currentUserId!!,
                            matchedUserId!!
                        )

                        ensureDocResult.onSuccess {
                            // Somente se o documento do chat for garantido, iniciamos as observações
                            startObservingChat()
                            updatePresenceUseCase(currentUserId!!, true)
                        }.onFailure { error ->
                            // Se houver um erro ao garantir o documento do chat, exibe um erro na UI
                            _uiState.value = ChatUiState.Error("Erro ao preparar chat: ${error.message}")
                        }

                    } else {
                        _uiState.value = ChatUiState.Error("Perfil do usuário correspondido não encontrado.")
                    }
                }?.onFailure { error ->
                    _uiState.value = ChatUiState.Error("Erro ao carregar dados do chat: ${error.message}")
                }
            } catch (e: Exception) {
                _uiState.value = ChatUiState.Error("Erro inesperado ao carregar dados do chat: ${e.message}")
            }
        }
    }

    /**
     * Inicia a observação de mensagens, presença e status de digitação em tempo real.
     * Combina múltiplos Flows para atualizar o [ChatUiState.Success] de forma reativa.
     */
    private fun startObservingChat() {
        if (matchId == null || currentUserId == null || matchedUserId == null || matchedUserProfile == null) {
            _uiState.value = ChatUiState.Error("Erro interno: Dados de chat incompletos para observação.")
            return
        }

        combine(
            observeMessagesUseCase(matchId!!),
            observePresenceUseCase(matchedUserId!!),
            observeTypingStatusUseCase(matchId!!, matchedUserId!!)
        ) { messages, isOnline, isTyping ->
            messages.filter { it.senderId == matchedUserId && it.status != "seen" }.forEach { message ->
                viewModelScope.launch {
                    updateMessageStatusUseCase(matchId!!, message.id, "seen")
                }
            }
            ChatUiState.Success(messages, matchedUserProfile!!, isOnline, isTyping)
        }.onEach { uiState ->
            _uiState.value = uiState
        }.launchIn(viewModelScope)
    }

    /**
     * Calcula um ID de conversa consistente entre dois usuários, garantindo que
     * ambos os participantes usem o mesmo ID para a mesma conversa, independentemente
     * de quem a iniciou.
     * @param userId1 O UID do primeiro usuário.
     * @param userId2 O UID do segundo usuário.
     * @return O ID da conversa (ex: "uid1_uid2" ou "uid2_uid1", sempre em ordem alfabética).
     */
    private fun calculateMatchId(userId1: String, userId2: String): String {
        return if (userId1 < userId2) "${userId1}_${userId2}" else "${userId2}_${userId1}"
    }

    /**
     * Lida com a mudança no campo de entrada de mensagem.
     * Também atualiza o status de digitação do usuário logado.
     * @param newMessage O novo texto do campo de entrada.
     */
    fun onMessageInputChanged(newMessage: String) {
        _messageInput.value = newMessage
        viewModelScope.launch {
            if (matchId != null && currentUserId != null) {
                updateTypingStatusUseCase(matchId!!, currentUserId!!, newMessage.isNotBlank())
            }
        }
    }

    /**
     * Envia uma nova mensagem para a conversa.
     * Cria um objeto [Message] e o envia usando o [SendMessageUseCase].
     * Limpa o campo de entrada e redefine o status de digitação após o envio.
     */
    fun sendMessage() {
        viewModelScope.launch {
            val messageContent = _messageInput.value.trim()
            if (messageContent.isNotBlank() && currentUserId != null && matchId != null) {
                val message = Message(
                    id = UUID.randomUUID().toString(),
                    senderId = currentUserId!!,
                    content = messageContent,
                    timestamp = System.currentTimeMillis(),
                    type = "text",
                    status = "sent"
                )
                val result = sendMessageUseCase(matchId!!, message)
                result.onSuccess {
                    _messageInput.value = ""
                    updateTypingStatusUseCase(matchId!!, currentUserId!!, false)
                }.onFailure { error ->
                    _uiState.value = ChatUiState.Error("Erro ao enviar mensagem: ${error.message}")
                }
            }
        }
    }

    /**
     * Chamado quando o ViewModel está prestes a ser destruído.
     * Garante que o status de presença do usuário seja definido como offline
     * e o status de digitação seja limpo para manter a consistência dos dados no backend.
     */
    override fun onCleared() {
        super.onCleared()
        viewModelScope.launch {
            if (currentUserId != null) {
                updatePresenceUseCase(currentUserId!!, false)
            }
            if (matchId != null && currentUserId != null) {
                updateTypingStatusUseCase(matchId!!, currentUserId!!, false)
            }
        }
    }
}