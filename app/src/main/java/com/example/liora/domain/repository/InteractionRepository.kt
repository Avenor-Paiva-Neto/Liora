package com.example.liora.domain.repository

import com.example.liora.domain.model.UserProfile
import kotlinx.coroutines.flow.Flow

/**
 * Interface que define o contrato para TODAS as operações de dados relacionadas
 * a interações entre usuários (likes, dislikes, etc.).
 */
interface InteractionRepository {

    suspend fun recordInteraction(fromUserId: String, toUserId: String, action: String): Result<Unit>

    suspend fun checkForMatch(userA_id: String, userB_id: String): Result<Boolean>

    suspend fun getPreviouslyInteractedUserIds(currentUserId: String): Result<List<String>>

    /**
     * Busca os perfis completos de todos os usuários que deram 'like'
     * no usuário atual.
     *
     * @param currentUserId O ID do usuário logado.
     * @return Um [Result] contendo a lista de [UserProfile] de quem o curtiu.
     */
    suspend fun getLikerProfiles(currentUserId: String): Result<List<UserProfile>>

    suspend fun deleteInteraction(fromUserId: String, toUserId: String): Result<Unit>

    /**
     * Observa os IDs dos usuários com quem o usuário atual teve um match mútuo.
     *
     * **MODIFICADO:** Agora retorna um [Flow] de uma lista de [Pair]<String, String>,
     * onde o primeiro [String] é o UID do usuário com match e o segundo [String]
     * é o `chatId` (que corresponde ao `matchDocumentId`).
     *
     * @param currentUserId O ID do usuário logado.
     * @return Um [Flow] de uma lista de [Pair]s, onde cada Pair contém o UID do usuário
     * com match e o ID do chat correspondente.
     */
    fun observeMutualMatches(currentUserId: String): Flow<List<Pair<String, String>>> // ALTERADO AQUI

    /**
     * NOVO MÉTODO: Registra um match mútuo entre dois usuários na coleção de matches.
     *
     * @param user1Id O ID do primeiro usuário envolvido no match.
     * @param user2Id O ID do segundo usuário envolvido no match.
     * @return Um [Result] indicando o sucesso ou falha da operação.
     */
    suspend fun createMatch(user1Id: String, user2Id: String): Result<Unit>
}