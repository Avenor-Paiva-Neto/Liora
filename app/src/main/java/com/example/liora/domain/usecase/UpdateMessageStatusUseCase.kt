package com.example.liora.domain.usecase

import com.example.liora.domain.repository.ChatRepository

/**
 * Caso de uso para atualizar o status de uma mensagem de chat.
 *
 * Esta classe encapsula a lógica de negócio para modificar o status de uma mensagem
 * específica em uma conversa (por exemplo, de "sent" para "delivered" ou "seen").
 * Ela delega a operação de atualização ao [ChatRepository].
 *
 * Adere ao Princípio da Responsabilidade Única (SRP) e ao Princípio da Inversão de Dependência (DIP).
 *
 * @param chatRepository O repositório de chat, injetado para realizar a operação de atualização.
 */
class UpdateMessageStatusUseCase(
    private val chatRepository: ChatRepository
) {
    /**
     * Permite que a instância de [UpdateMessageStatusUseCase] seja chamada como uma função.
     *
     * Delega a operação de atualização de status para o [chatRepository] e retorna o resultado.
     *
     * @param matchId O identificador da conversa onde a mensagem está localizada.
     * @param messageId O identificador único da mensagem cujo status será atualizado.
     * @param status O novo status a ser aplicado à mensagem (e.g., "sent", "delivered", "seen").
     * @return [Result]<Unit> Indicando sucesso ou falha da operação.
     */
    suspend operator fun invoke(matchId: String, messageId: String, status: String): Result<Unit> {
        return chatRepository.updateMessageStatus(matchId, messageId, status)
    }
}
