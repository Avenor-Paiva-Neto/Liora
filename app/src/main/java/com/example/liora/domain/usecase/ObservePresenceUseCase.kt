package com.example.liora.domain.usecase

import com.example.liora.domain.repository.PresenceRepository
import kotlinx.coroutines.flow.Flow

/**
 * Caso de uso para observar o status de presença de um usuário em tempo real.
 *
 * Esta classe encapsula a lógica de negócio para fornecer um fluxo reativo
 * do status online/offline de um usuário específico. É essencial para a
 * funcionalidade de chat em tempo real, permitindo que a UI exiba o status
 * do interlocutor de forma dinâmica e instantânea.
 *
 * Adere ao Princípio da Responsabilidade Única (SRP) e ao Princípio da Inversão de Dependência (DIP).
 *
 * @param presenceRepository O repositório de presença, injetado para realizar a operação de observação.
 */
class ObservePresenceUseCase(
    private val presenceRepository: PresenceRepository
) {
    /**
     * Permite que a instância de [ObservePresenceUseCase] seja chamada como uma função.
     *
     * Delega a responsabilidade de observar a presença ao [presenceRepository] e
     * retorna o [Flow] diretamente. Este Flow emitirá `true` quando o usuário
     * estiver online e `false` quando estiver offline, atualizando automaticamente
     * qualquer coletor na UI.
     *
     * @param userId O identificador único do usuário cujo status de presença será observado.
     * @return [Flow]<Boolean> Um fluxo contínuo do status de presença (`true` para online, `false` para offline).
     */
    operator fun invoke(userId: String): Flow<Boolean> {
        return presenceRepository.observePresence(userId)
    }
}
