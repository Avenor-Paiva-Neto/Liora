package com.example.liora.domain.usecase

import com.example.liora.domain.compatibility.CompatibilityScorer
import com.example.liora.domain.compatibility.GenderInferenceEngine
import com.example.liora.domain.model.Gender
import com.example.liora.domain.model.UserProfile
import com.example.liora.domain.repository.InteractionRepository
import com.example.liora.domain.repository.UserRepository

class FindMatchesUseCase(
    private val userRepository: UserRepository,
    private val scorer: CompatibilityScorer,
    // NOVA DEPENDÊNCIA: Precisamos saber com quem o usuário já interagiu.
    private val interactionRepository: InteractionRepository
) {
    /**
     * Executa a busca por novos perfis, agora com a inteligência de
     * remover perfis com os quais o usuário já interagiu.
     */
    suspend operator fun invoke(currentUserProfile: UserProfile): Result<List<UserProfile>> {
        return try {
            // --- ETAPA 1: Buscar IDs de usuários já vistos ---
            val seenUserIdsResult = interactionRepository.getPreviouslyInteractedUserIds(currentUserProfile.uid)
            val seenUserIds = seenUserIdsResult.getOrThrow() // Se falhar aqui, o catch trata.

            // --- ETAPA 2: Buscar candidatos potenciais (como antes) ---
            val candidatesResult = userRepository.getPotentialCandidates(currentUserProfile)
            val allCandidates = candidatesResult.getOrThrow()

            if (allCandidates.isEmpty()) {
                return Result.success(emptyList())
            }

            // --- ETAPA 3: Filtrar os perfis já vistos da lista de candidatos ---
            val newCandidates = allCandidates.filterNot { candidate ->
                seenUserIds.contains(candidate.uid)
            }

            if (newCandidates.isEmpty()) {
                return Result.success(emptyList())
            }

            // --- ETAPA 4: Aplicar a lógica de match (gênero, score, etc.) apenas nos perfis NOVOS ---
            val currentUserGender = GenderInferenceEngine.infer(
                sexuality = currentUserProfile.sexuality,
                lookingFor = currentUserProfile.preferences.lookingFor
            )

            val potentialMatches = newCandidates.filter { candidate ->
                val candidateGender = GenderInferenceEngine.infer(candidate.sexuality, candidate.preferences.lookingFor)
                isMatch(
                    userA_gender = currentUserGender,
                    userA_lookingFor = currentUserProfile.preferences.lookingFor,
                    userB_gender = candidateGender
                ) && isMatch(
                    userA_gender = candidateGender,
                    userA_lookingFor = candidate.preferences.lookingFor,
                    userB_gender = currentUserGender
                )
            }

            val scoredResults = potentialMatches.map { candidate ->
                scorer.calculate(currentUserProfile, candidate) to candidate
            }

            val dailyLimit = 30
            val finalList = scoredResults
                .filter { (result, _) -> !result.isVetoed && result.score >= 60 }
                .sortedByDescending { (result, _) -> result.score }
                .take(dailyLimit)
                .map { (_, profile) -> profile }

            Result.success(finalList)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun isMatch(userA_gender: Gender, userA_lookingFor: String, userB_gender: Gender): Boolean {
        if (userA_gender == Gender.INDEFINIDO || userB_gender == Gender.INDEFINIDO) return false

        return when (userA_lookingFor) {
            "Mulher" -> userB_gender == Gender.MULHER
            "Homen" -> userB_gender == Gender.HOMEM
            "Outros" -> true
            else -> false
        }
    }
}