package com.example.liora.domain.repository

import kotlinx.coroutines.flow.Flow

/**
 * Interface que define o contrato para as operações relacionadas ao status de presença
 * dos usuários (online/offline) e ao status de digitação (digitando/não digitando).
 *
 * Esta abstração é crucial para desacoplar a lógica de negócios que depende dessas
 * informações da implementação específica do serviço de presença (ex: Firebase Realtime Database).
 *
 * Adere ao Princípio da Inversão de Dependência (DIP) e ao Princípio da Segregação
 * de Interfaces (ISP).
 */
interface PresenceRepository {

    /**
     * Permite que um usuário atualize seu próprio status de presença (online ou offline).
     *
     * Tipicamente chamado quando o usuário entra/sai do aplicativo, ou quando o aplicativo
     * vai para o background/foreground.
     *
     * @param userId O ID do usuário cujo status de presença será atualizado.
     * @param isOnline `true` se o usuário estiver online, `false` caso contrário.
     * @return [Result]<Unit> Indicando sucesso ou falha da operação assíncrona.
     */
    suspend fun updatePresence(userId: String, isOnline: Boolean): Result<Unit>

    /**
     * Fornece um fluxo reativo que emite o status de presença de um usuário específico em tempo real.
     *
     * Isso permite que a UI exiba se o interlocutor está online ou offline.
     *
     * @param userId O ID do usuário a ser observado.
     * @return [Flow]<Boolean> Onde `true` significa online e `false` significa offline.
     */
    fun observePresence(userId: String): Flow<Boolean>

    /**
     * Permite que um usuário atualize seu status de digitação dentro de uma conversa específica.
     *
     * Usado para exibir o indicador "digitando..." para o outro participante.
     *
     * @param matchId O ID da conversa onde o status de digitação está sendo atualizado.
     * @param userId O ID do usuário que está digitando (ou parou de digitar).
     * @param isTyping `true` se o usuário estiver digitando, `false` caso contrário.
     * @return [Result]<Unit> Indicando sucesso ou falha da operação assíncrona.
     */
    suspend fun updateTypingStatus(matchId: String, userId: String, isTyping: Boolean): Result<Unit>

    /**
     * Fornece um fluxo reativo que emite o status de digitação de um usuário específico
     * dentro de uma conversa específica em tempo real.
     *
     * Isso permite que a UI exiba o indicador "digitando..." para o outro participante.
     *
     * @param matchId O ID da conversa a ser observada.
     * @param otherUserId O ID do usuário cujo status de digitação será observado.
     * @return [Flow]<Boolean> Onde `true` significa digitando e `false` significa não digitando.
     */
    fun observeTypingStatus(matchId: String, otherUserId: String): Flow<Boolean>
}
