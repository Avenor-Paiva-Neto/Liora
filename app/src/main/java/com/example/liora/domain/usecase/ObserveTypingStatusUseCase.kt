package com.example.liora.domain.usecase

import com.example.liora.domain.repository.PresenceRepository
import kotlinx.coroutines.flow.Flow

/**
 * Caso de uso para observar o status de digitação de um usuário em uma conversa específica em tempo real.
 *
 * Esta classe encapsula a lógica de negócio para fornecer um fluxo reativo
 * do status de digitação de um usuário. É essencial para a experiência de
 * usuário em tempo real, permitindo que a UI exiba o indicador "digitando..."
 * para o interlocutor.
 *
 * Adere ao Princípio da Responsabilidade Única (SRP) e ao Princípio da Inversão de Dependência (DIP).
 *
 * @param presenceRepository O repositório de presença, injetado para realizar a operação de observação.
 */
class ObserveTypingStatusUseCase(
    private val presenceRepository: PresenceRepository
) {
    /**
     * Permite que a instância de [ObserveTypingStatusUseCase] seja chamada como uma função.
     *
     * Delega a responsabilidade de observar o status de digitação ao [presenceRepository]
     * e retorna o [Flow] diretamente. Este Flow emitirá `true` quando o usuário
     * estiver digitando e `false` quando não estiver, atualizando automaticamente
     * qualquer coletor na UI.
     *
     * @param matchId O identificador único da conversa onde o status de digitação será observado.
     * @param otherUserId O identificador único do usuário cujo status de digitação será observado.
     * @return [Flow]<Boolean> Um fluxo contínuo do status de digitação (`true` para digitando, `false` para não digitando).
     */
    operator fun invoke(matchId: String, otherUserId: String): Flow<Boolean> {
        return presenceRepository.observeTypingStatus(matchId, otherUserId)
    }
}
