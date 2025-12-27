package com.example.liora.domain.usecase

import com.example.liora.domain.model.UserProfile
import com.example.liora.domain.repository.InteractionRepository
import com.example.liora.domain.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth

/**
 * Caso de uso para processar a ação de "pular" (dispensar) um perfil.
 *
 * Orquestra o registro da interação e a atualização do estado de "desfazer" do usuário.
 */
class ProcessSkipActionUseCase(
    private val interactionRepository: InteractionRepository,
    private val userRepository: UserRepository,
    private val auth: FirebaseAuth
) {

    /**
     * Executa a lógica de "pular" um perfil.
     *
     * @param skippedProfile O perfil que foi dispensado pelo usuário logado.
     * @return Um [Result] indicando o sucesso (Unit) ou a falha (Exception) da operação.
     */
    suspend operator fun invoke(skippedProfile: UserProfile): Result<Unit> {
        return try {
            val currentUserId = auth.currentUser?.uid
                ?: return Result.failure(IllegalStateException("Usuário não autenticado."))

            // Etapa 1: Registrar a interação de "dislike".
            interactionRepository.recordInteraction(
                fromUserId = currentUserId,
                toUserId = skippedProfile.uid,
                action = "dislike"
            ).onFailure {
                return Result.failure(it)
            }

            // Etapa 2: Atualizar o estado de "desfazer" do usuário.
            // --- CORREÇÃO APLICADA AQUI ---
            // Criamos o mapa com os dados a serem atualizados.
            val undoStateUpdate = mapOf("lastSkippedProfileId" to skippedProfile.uid)

            // Chamamos o método com a nova assinatura, passando o mapa.
            userRepository.updateUndoState(
                userId = currentUserId,
                undoStateUpdate = undoStateUpdate
            ).onFailure {
                return Result.failure(it)
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}