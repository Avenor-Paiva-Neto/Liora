package com.example.liora.domain.usecase

import com.example.liora.domain.repository.PresenceRepository

/**
 * Caso de uso para atualizar o status de digitação de um usuário em uma conversa específica.
 *
 * Esta classe encapsula a lógica de negócio para modificar o status de digitação
 * de um usuário, delegando a operação ao [PresenceRepository]. É crucial para a
 * experiência de usuário em tempo real, fornecendo feedback visual sobre a
 * atividade do interlocutor.
 *
 * Adere ao Princípio da Responsabilidade Única (SRP) e ao Princípio da Inversão de Dependência (DIP).
 *
 * @param presenceRepository O repositório de presença, injetado para realizar a operação de atualização.
 */
class UpdateTypingStatusUseCase(
    private val presenceRepository: PresenceRepository
) {
    /**
     * Permite que a instância de [UpdateTypingStatusUseCase] seja chamada como uma função.
     *
     * Delega a operação de atualização do status de digitação para o [presenceRepository]
     * e retorna o resultado.
     *
     * @param matchId O identificador único da conversa onde o status de digitação será atualizado.
     * @param userId O identificador único do usuário cujo status de digitação será atualizado.
     * @param isTyping Um valor booleano que indica se o usuário está digitando (`true`) ou não (`false`).
     * @return [Result]<Unit> Indicando sucesso ou falha da operação.
     */
    suspend operator fun invoke(matchId: String, userId: String, isTyping: Boolean): Result<Unit> {
        return presenceRepository.updateTypingStatus(matchId, userId, isTyping)
    }
}
