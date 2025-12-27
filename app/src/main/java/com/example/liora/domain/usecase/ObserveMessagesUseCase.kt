package com.example.liora.domain.usecase

import com.example.liora.domain.model.Message
import com.example.liora.domain.repository.ChatRepository
import kotlinx.coroutines.flow.Flow

/**
 * Caso de uso para observar mensagens de chat em tempo real.
 *
 * Esta classe encapsula a lógica de negócio para fornecer um fluxo contínuo de mensagens
 * para uma conversa específica, permitindo que a UI exiba as mensagens em tempo real.
 *
 * Adere ao Princípio da Responsabilidade Única (SRP) e ao Princípio da Inversão de Dependência (DIP).
 *
 * @param chatRepository O repositório de chat, injetado para realizar a operação de observação.
 */
class ObserveMessagesUseCase(
    private val chatRepository: ChatRepository
) {
    /**
     * Permite que a instância de [ObserveMessagesUseCase] seja chamada como uma função.
     *
     * Delega a responsabilidade de observar as mensagens ao [chatRepository] e retorna o [Flow] diretamente.
     * Qualquer componente que "coletar" este Flow receberá automaticamente atualizações sempre que
     * a lista de mensagens na fonte de dados subjacente mudar.
     *
     * @param matchId O identificador único da conversa (sala de chat) cujas mensagens serão observadas.
     * @return [Flow]<List<Message>> Um fluxo contínuo da lista de mensagens da conversa.
     */
    operator fun invoke(matchId: String): Flow<List<Message>> {
        return chatRepository.observeMessages(matchId)
    }
}
