package com.example.liora.domain.usecase

import com.example.liora.domain.repository.PresenceRepository

/**
 * Caso de uso para atualizar o status de presença de um usuário.
 *
 * Esta classe encapsula a lógica de negócio para modificar o status online/offline
 * de um usuário, delegando a operação ao [PresenceRepository].
 *
 * Adere ao Princípio da Responsabilidade Única (SRP) e ao Princípio da Inversão de Dependência (DIP).
 *
 * @param presenceRepository O repositório de presença, injetado para realizar a operação de atualização.
 */
class UpdatePresenceUseCase(
    private val presenceRepository: PresenceRepository
) {
    /**
     * Permite que a instância de [UpdatePresenceUseCase] seja chamada como uma função.
     *
     * Delega a operação de atualização de presença para o [presenceRepository] e retorna o resultado.
     *
     * @param userId O identificador único do usuário cujo status de presença será atualizado.
     * @param isOnline Um valor booleano que indica se o usuário está online (`true`) ou offline (`false`).
     * @return [Result]<Unit> Indicando sucesso ou falha da operação.
     */
    suspend operator fun invoke(userId: String, isOnline: Boolean): Result<Unit> {
        return presenceRepository.updatePresence(userId, isOnline)
    }
}
