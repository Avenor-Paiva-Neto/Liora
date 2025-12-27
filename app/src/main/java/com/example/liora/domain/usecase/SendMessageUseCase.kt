package com.example.liora.domain.usecase

import com.example.liora.domain.model.Message
import com.example.liora.domain.repository.ChatRepository

/**
 * Caso de uso para enviar uma mensagem de chat.
 *
 * Esta classe encapsula a lógica de negócio específica para o envio de mensagens.
 * Ela orquestra a operação, delegando a persistência da mensagem ao [ChatRepository].
 *
 * Adere ao Princípio da Responsabilidade Única (SRP) e ao Princípio da Inversão de Dependência (DIP).
 *
 * @param chatRepository O repositório de chat, injetado para realizar a operação de envio.
 */
class SendMessageUseCase(
    private val chatRepository: ChatRepository
) {
    /**
     * Permite que a instância de [SendMessageUseCase] seja chamada como uma função.
     *
     * Delega a operação de envio de mensagem para o [chatRepository] e retorna o resultado.
     *
     * @param matchId O identificador único da conversa para a qual a mensagem será enviada.
     * @param message O objeto [Message] contendo os dados da mensagem a ser enviada.
     * @return [Result]<Unit> Indicando sucesso ou falha da operação.
     */
    suspend operator fun invoke(matchId: String, message: Message): Result<Unit> {
        return chatRepository.sendMessage(matchId, message)
    }
}
