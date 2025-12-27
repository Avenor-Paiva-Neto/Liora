package com.example.liora.domain.usecase

import com.example.liora.domain.model.MatchResult
import com.example.liora.domain.model.UserProfile
import com.example.liora.domain.repository.InteractionRepository
import com.google.firebase.auth.FirebaseAuth

/**
 * Caso de uso responsável por processar a ação de "like" de um usuário em um perfil.
 *
 * Ele orquestra o registro da interação e a verificação subsequente de um match mútuo,
 * encapsulando as regras de negócio de forma limpa.
 */
class ProcessLikeActionUseCase(
    private val interactionRepository: InteractionRepository,
    private val auth: FirebaseAuth
) {

    /**
     * Executa o caso de uso.
     *
     * @param likedProfile O perfil que o usuário logado acabou de curtir.
     * @return Um [Result] contendo o [MatchResult] (com a informação se deu match e o perfil correspondido)
     * ou uma [Exception] em caso de falha.
     */
    suspend operator fun invoke(likedProfile: UserProfile): Result<MatchResult> {
        return try {
            // 1. Garante que temos um usuário logado para atribuir a ação.
            val currentUserId = auth.currentUser?.uid
                ?: return Result.failure(IllegalStateException("Usuário não autenticado."))

            // 2. Registra a interação de "like" no nosso novo sistema.
            interactionRepository.recordInteraction(
                fromUserId = currentUserId,
                toUserId = likedProfile.uid,
                action = "like" // A ação é explicitamente um "like".
            ).onFailure {
                // Se o registro da interação falhar, propaga o erro e interrompe o fluxo.
                return Result.failure(it)
            }

            // 3. Após registrar a curtida, verifica se um match foi formado.
            val matchCheckResult = interactionRepository.checkForMatch(
                userA_id = currentUserId,
                userB_id = likedProfile.uid
            )

            val isMatch = matchCheckResult.getOrThrow() // Se a verificação falhar, lança exceção para o catch.

            if (isMatch) {
                // 4a. Deu match! Agora, registra o match na nova coleção 'matches'.
                // É crucial que isso ocorra AQUI, após a confirmação do match mútuo.
                interactionRepository.createMatch(
                    user1Id = currentUserId,
                    user2Id = likedProfile.uid
                ).onFailure {
                    // Se o registro do match falhar, propaga o erro.
                    return Result.failure(it)
                }

                // Retorna sucesso com o perfil correspondido.
                Result.success(MatchResult(isMatch = true, matchedProfile = likedProfile))
            } else {
                // 4b. Não deu match. Retorna sucesso, mas indicando que não houve match.
                Result.success(MatchResult(isMatch = false))
            }

        } catch (e: Exception) {
            // Captura qualquer exceção durante o processo e a encapsula no Result.
            Result.failure(e)
        }
    }
}