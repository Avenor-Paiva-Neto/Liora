package com.example.liora.domain.model

/**
 * Representa o resultado de uma ação de "like".
 *
 * Esta classe de dados informa se a ação resultou em um match mútuo
 * e, em caso afirmativo, carrega os dados do perfil com quem o match ocorreu.
 *
 * @property isMatch Indica se a curtida resultou em um match (true) ou não (false).
 * @property matchedProfile Contém o perfil completo do usuário correspondido, caso isMatch seja true.
 * É nulo se não houver match.
 */
data class MatchResult(
    val isMatch: Boolean,
    val matchedProfile: UserProfile? = null
)