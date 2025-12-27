package com.example.liora.domain.compatibility

import com.example.liora.domain.model.*
import kotlin.math.abs

/**
 * O coração da IA do Liora.
 * Este objeto é responsável por receber dois perfis e calcular
 * o quão compatíveis eles são, gerando um resultado detalhado.
 */
object CompatibilityScorer {

    // --- CONSTANTES DE PESO ---
    // Centralizamos os pesos aqui para facilitar o ajuste fino da IA no futuro.
    private const val PERSONALITY_WEIGHT = 0.40 // 40% do score total vem da personalidade
    private const val LIFESTYLE_WEIGHT = 0.35 // 35% do estilo de vida
    private const val VALUES_WEIGHT = 0.25    // 25% dos valores (fidelidade, etc.)

    fun calculate(profileA: UserProfile, profileB: UserProfile): CompatibilityResult {
        // ETAPA 1: FILTRO DE DEAL-BREAKERS
        if (checkDealBreakers(profileA, profileB)) {
            return CompatibilityResult(
                score = 0,
                keyStrengths = emptyList(),
                potentialConflicts = listOf("Incompatibilidade fundamental encontrada."),
                isVetoed = true
            )
        }

        // ===================================================================
        // ETAPA 2: CÁLCULO DE SCORE PONDERADO (AGORA COMPLETO)
        // ===================================================================
        val strengths = mutableListOf<String>()
        val conflicts = mutableListOf<String>()

        val personalityScore = calculatePersonalityScore(profileA.psycheProfile, profileB.psycheProfile)
        val lifestyleScore = calculateLifestyleScore(profileA.psycheProfile, profileB.psycheProfile, strengths, conflicts)
        val valuesScore = calculateValuesScore(profileA.psycheProfile, profileB.psycheProfile, strengths)

        // Cálculo Final Ponderado
        val finalScore = (personalityScore * PERSONALITY_WEIGHT) +
                (lifestyleScore * LIFESTYLE_WEIGHT) +
                (valuesScore * VALUES_WEIGHT)

        return CompatibilityResult(
            score = finalScore.toInt().coerceIn(0, 100),
            keyStrengths = strengths.distinct(), // .distinct() para remover duplicados
            potentialConflicts = conflicts.distinct()
        )
    }

    // --- FUNÇÕES DE AJUDA PARA A ETAPA 2 ---

    /**
     * Calcula o score de compatibilidade de personalidade (0-100).
     */
    private fun calculatePersonalityScore(psycheA: PsycheProfile, psycheB: PsycheProfile): Int {
        val vectorA = psycheA.personalityVector
        val vectorB = psycheB.personalityVector
        // Para cada traço, calculamos um score de 0 a 20, para que a soma total seja 100.
        val opennessScore = calculateTraitScore(vectorA.openness, vectorB.openness, isComplementary = false)
        val conscientiousnessScore = calculateTraitScore(vectorA.conscientiousness, vectorB.conscientiousness, isComplementary = false)
        val extraversionScore = calculateTraitScore(vectorA.extraversion, vectorB.extraversion, isComplementary = true)
        val agreeablenessScore = calculateTraitScore(vectorA.agreeableness, vectorB.agreeableness, isComplementary = false)
        val neuroticismScore = calculateTraitScore(vectorA.neuroticism, vectorB.neuroticism, isComplementary = true)
        return opennessScore + conscientiousnessScore + extraversionScore + agreeablenessScore + neuroticismScore
    }

    /**
     * Função genérica para pontuar um único traço de personalidade (retorna 0-20).
     */
    private fun calculateTraitScore(scoreA: Int, scoreB: Int, isComplementary: Boolean): Int {
        val difference = abs(scoreA - scoreB)
        return if (isComplementary) {
            when {
                difference > 60 -> 20; difference > 30 -> 15; difference > 15 -> 8; else -> 2
            }
        } else {
            when {
                difference < 15 -> 20; difference < 30 -> 12; difference < 50 -> 5; else -> 0
            }
        }
    }

    /**
     * Calcula o score de compatibilidade de estilo de vida (0-100).
     */
    private fun calculateLifestyleScore(
        psycheA: PsycheProfile,
        psycheB: PsycheProfile,
        strengths: MutableList<String>,
        conflicts: MutableList<String>
    ): Int {
        var score = 0
        val maxScore = 105 // Soma de todos os pontos possíveis (7 * 15)

        // Comparamos cada dimensão de estilo de vida
        if (psycheA.lifePace == psycheB.lifePace) {
            score += 15
            strengths.add("Possuem um ritmo de vida similar")
        } else if (abs(psycheA.lifePace.ordinal - psycheB.lifePace.ordinal) > 1) {
            score -= 10
            conflicts.add("Ritmos de vida podem ser um desafio")
        }

        if (psycheA.socialStyle == psycheB.socialStyle) { score += 15; strengths.add("Preferem atividades sociais parecidas") }
        if (psycheA.planningStyle == psycheB.planningStyle) { score += 15; strengths.add("Lidam com planeamento de forma semelhante") }
        if (psycheA.lifeFocus == psycheB.lifeFocus) { score += 15 }
        if (psycheA.comfortZone == psycheB.comfortZone) { score += 15; strengths.add("Sentem-se confortáveis nos mesmos ambientes") }
        if (psycheA.passionTopic == psycheB.passionTopic) { score += 15; strengths.add("Gostam de conversar sobre os mesmos assuntos") }
        if (psycheA.idealDate == psycheB.idealDate) { score += 15; strengths.add("Têm uma ideia parecida de encontro ideal") }

        // Normaliza o score para uma escala de 0 a 100
        return ((score.coerceIn(0, maxScore) / maxScore.toFloat()) * 100).toInt()
    }

    /**
     * Calcula o score de compatibilidade de valores (0-100).
     */
    private fun calculateValuesScore(
        psycheA: PsycheProfile,
        psycheB: PsycheProfile,
        strengths: MutableList<String>
    ): Int {
        var score = 0
        if (psycheA.fidelityStyle == psycheB.fidelityStyle) {
            score += 50
            strengths.add("Têm a mesma visão sobre fidelidade")
        }
        if (psycheA.relationshipGoal == psycheB.relationshipGoal) {
            score += 50
            strengths.add("Buscam o mesmo tipo de relacionamento")
        }
        return score // O score já é de 0 a 100
    }


    // --- FUNÇÕES DE AJUDA PARA A ETAPA 1 (DEAL-BREAKERS) ---
    private fun checkDealBreakers(profileA: UserProfile, profileB: UserProfile): Boolean {
        val fidelityMismatch1 = profileA.psycheProfile.fidelityStyle == FidelityStyle.STRICT &&
                profileB.psycheProfile.fidelityStyle == FidelityStyle.UNIMPORTANT
        val fidelityMismatch2 = profileB.psycheProfile.fidelityStyle == FidelityStyle.STRICT &&
                profileA.psycheProfile.fidelityStyle == FidelityStyle.UNIMPORTANT
        if (fidelityMismatch1 || fidelityMismatch2) return true
        if (hasConflictingValues(profileA.psycheProfile.coreValues, profileB)) return true
        if (hasConflictingValues(profileB.psycheProfile.coreValues, profileA)) return true
        return false
    }

    private fun hasConflictingValues(unacceptableValues: List<String>, targetProfile: UserProfile): Boolean {
        val targetPersonality = targetProfile.psycheProfile.personalityVector
        for (value in unacceptableValues) {
            when (value) {
                "Desonestidade" -> if (targetPersonality.agreeableness < 30) return true
                "Falta de ambição" -> if (targetPersonality.conscientiousness < 30) return true
                "Dependência emocional extrema" -> if (targetPersonality.neuroticism > 70) return true
                "Arrogância ou ego inflado" -> if (targetPersonality.agreeableness < 25) return true
                "Ciúmes e controle excessivo" -> if (targetPersonality.neuroticism > 75) return true
            }
        }
        return false
    }
}