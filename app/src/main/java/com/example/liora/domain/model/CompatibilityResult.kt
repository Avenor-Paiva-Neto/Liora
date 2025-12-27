package com.example.liora.domain.model

/**
 * Representa o resultado completo e detalhado do cálculo de compatibilidade
 * entre dois perfis.
 *
 * Em vez de apenas um número, este objeto nos dá insights para mostrar ao usuário.
 */
data class CompatibilityResult(
    val score: Int,                         // A pontuação final de 0 a 100
    val keyStrengths: List<String>,         // Uma lista de pontos fortes. Ex: "Ambos amam o conforto do lar"
    val potentialConflicts: List<String>,   // Uma lista de possíveis pontos de atrito. Ex: "Um é muito festeiro e o outro, caseiro"
    val isVetoed: Boolean = false           // Indica se o match foi vetado por um "deal-breaker"
)