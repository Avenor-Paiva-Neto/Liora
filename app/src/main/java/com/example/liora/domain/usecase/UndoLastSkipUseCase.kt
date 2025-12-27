package com.example.liora.domain.usecase

import com.example.liora.domain.model.UserProfile
import com.example.liora.domain.repository.InteractionRepository
import com.example.liora.domain.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import java.util.Calendar
import java.util.Date
import java.util.concurrent.TimeUnit

/**
 * Caso de uso para a funcionalidade de "desfazer a última ação de pular".
 *
 * Ele verifica se o usuário tem permissão para usar a funcionalidade com base
 * no seu nível de assinatura (de forma modular) e, em caso afirmativo, reverte a
 * última interação de "dislike" e retorna o perfil correspondente.
 */
class UndoLastSkipUseCase(
    private val userRepository: UserRepository,
    private val interactionRepository: InteractionRepository,
    private val auth: FirebaseAuth
) {

    suspend operator fun invoke(): Result<UserProfile?> {
        return try {
            val currentUserId = auth.currentUser?.uid
                ?: return Result.failure(IllegalStateException("Usuário não autenticado."))

            // 1. Busca o perfil completo do usuário, que contém o 'undoState' e o 'subscriptionTier'.
            val currentUserProfile = userRepository.getUserData(currentUserId).getOrThrow()
                ?: return Result.failure(IllegalStateException("Perfil do usuário não encontrado."))

            // 2. Verifica se há uma ação para ser desfeita.
            val lastSkippedId = currentUserProfile.undoState["lastSkippedProfileId"] as? String
            if (lastSkippedId.isNullOrEmpty()) {
                return Result.success(null) // Retorna sucesso com 'null' para indicar "nada a desfazer".
            }

            // 3. Verifica se o usuário PODE executar a ação (lógica de assinatura).
            if (!canPerformUndo(currentUserProfile)) {
                return Result.failure(Exception("Limite de 'desfazer' atingido."))
            }

            // 4. Se permitido, reverte a interação.
            interactionRepository.deleteInteraction(fromUserId = currentUserId, toUserId = lastSkippedId)
                .onFailure { return Result.failure(it) }

            // 5. Atualiza o estado de "desfazer" do usuário.
            val newUndoState = mapOf(
                "lastSkippedProfileId" to null, // Limpa o último perfil pulado.
                "lastUndoTimestamp" to Date()   // Registra quando o "desfazer" foi usado.
                // No futuro, aqui também atualizaríamos o 'dailyUndoCount'.
            )
            userRepository.updateUndoState(currentUserId, newUndoState)
                .onFailure { return Result.failure(it) }

            // 6. Busca e retorna o perfil que foi "resgatado".
            val restoredProfile = userRepository.getUserData(lastSkippedId).getOrThrow()
            Result.success(restoredProfile)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Função privada que encapsula a lógica de permissão baseada em assinatura.
     * Ela é MODULAR: funcional agora e pronta para ser expandida.
     */
    private fun canPerformUndo(profile: UserProfile): Boolean {
        // Lógica para o futuro sistema de assinatura.
        // Por enquanto, vamos simular a lógica de não-assinante (1 vez por semana).

        val lastUndoTimestamp = (profile.undoState["lastUndoTimestamp"] as? com.google.firebase.Timestamp)?.toDate()
            ?: return true // Se nunca usou, pode usar.

        val currentTime = Calendar.getInstance().time
        val diffInMillis = currentTime.time - lastUndoTimestamp.time
        val diffInDays = TimeUnit.MILLISECONDS.toDays(diffInMillis)

        return when (profile.subscriptionTier.uppercase()) {
            "PREMIUM" -> true // Assinantes premium sempre podem.
            "MEDIUM" -> {
                // TODO: Implementar lógica de 10 por dia quando o 'undoState' tiver os campos.
                true // Por enquanto, permitimos.
            }
            else -> { // FREE ou qualquer outro valor
                // Permite se já se passaram 7 dias.
                diffInDays >= 7
            }
        }
    }
}