package com.example.liora.domain.usecase

import com.example.liora.domain.model.UserProfile
import com.example.liora.domain.repository.InteractionRepository
import com.google.firebase.auth.FirebaseAuth

/**
 * Caso de uso dedicado a buscar a lista de perfis que curtiram o usuário logado.
 *
 * Encapsula a lógica de negócio, mantendo o ViewModel limpo e focado no estado da UI.
 */
class GetLikerProfilesUseCase(
    private val interactionRepository: InteractionRepository,
    private val auth: FirebaseAuth
) {
    suspend operator fun invoke(): Result<List<UserProfile>> {
        return try {
            val currentUserId = auth.currentUser?.uid
                ?: return Result.failure(IllegalStateException("Usuário não autenticado."))

            interactionRepository.getLikerProfiles(currentUserId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}