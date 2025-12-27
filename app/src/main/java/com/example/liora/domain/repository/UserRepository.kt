package com.example.liora.domain.repository

import com.example.liora.domain.model.UserProfile
import kotlinx.coroutines.flow.Flow
import java.util.Date

/**
 * Interface que define o contrato para todas as operações de dados
 * relacionadas ao perfil do usuário.
 */
interface UserRepository {

    suspend fun saveUserData(user: UserProfile): Result<Unit>

    suspend fun getUserData(userId: String): Result<UserProfile?>

    suspend fun getPotentialCandidates(currentUserProfile: UserProfile): Result<List<UserProfile>>

    /**
     * VERSÃO FINAL E CORRIGIDA: Atualiza campos dentro do mapa 'undoState' de um usuário.
     *
     * Usar um mapa torna o método flexível e preparado para futuras adições
     * (como contadores de 'undo'), sem precisar alterar a assinatura novamente.
     *
     * @param userId O ID do usuário a ser atualizado.
     * @param undoStateUpdate O mapa contendo os campos do 'undoState' a serem atualizados.
     * Ex: mapOf("lastSkippedProfileId" to "some_id", "lastUndoTimestamp" to Date())
     * @return Um [Result] indicando o sucesso ou a falha da operação.
     */
    suspend fun updateUndoState(userId: String, undoStateUpdate: Map<String, Any?>): Result<Unit>

    /**
     * NOVO MÉTODO: Observa os perfis de usuários dado uma lista de IDs.
     *
     * Este método é utilizado pelo Use Case para obter os perfis completos dos usuários
     * com quem o usuário atual teve um match, após seus IDs serem fornecidos
     * pelo InteractionRepository.
     *
     * @param userIds Uma lista de IDs de usuários cujos perfis devem ser observados.
     * @return Um [Flow] de uma lista de [UserProfile] correspondentes aos IDs fornecidos.
     */
    fun observeUserProfilesByIds(userIds: List<String>): Flow<List<UserProfile>>
}