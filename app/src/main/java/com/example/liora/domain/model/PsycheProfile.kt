// Caminho: com.example.liora.domain.model

package com.example.liora.domain.model

// ===================================================================
// ENUMS DA TELA "SOBRE VOCÊ"
// ===================================================================
enum class LifePace { AGITADO, EQUILIBRADO, TRANQUILO, ADAPTAVEL, INDEFINIDO }
enum class SocialStyle { CASEIRO, AVENTUREIRO, SOCIAL, INDEPENDENTE, INDEFINIDO }
enum class PlanningStyle { PLANEJADOR, FLEXIVEL, FLUIDO, ADAPTAVEL, INDEFINIDO }
enum class Focus { TRABALHO, ESTUDO_E_EQUILIBRIO, EQUILIBRIO, TRANSICAO, INDEFINIDO }
enum class ComfortZone { CASA, EXPLORACAO, SOCIAL, NATUREZA, INDEFINIDO }

// ===================================================================
// ENUMS DA TELA "HOBBIES"
// ===================================================================
enum class FreeTimeActivity { MEDIA_CONSUMER, INTELLECTUAL, GAMER, SOCIAL, CREATOR, OTHER, UNKNOWN }
enum class LeisureType { CULTURE, SPORTS, NATURE_TRAVEL, PARTY, RELAX, UNKNOWN }
enum class PassionTopic { MUSIC, MEDIA, GAMES, PHILOSOPHY, TECHNOLOGY, POP_CULTURE, UNKNOWN }
enum class IdealExperience { UNUSUAL_TRAVEL, COOKING, GAME_NIGHT, CREATIVE_PROJECT, QUIET_COMFORT, UNKNOWN }
enum class SelfPerception { RESTLESS_MIND, MOMENT_COLLECTOR, STORY_LOVER, HUMOROUS, OWN_RHYTHM, UNKNOWN }
enum class IdealDate { SIMPLE_CONVERSATION, UNIQUE_ACTIVITY, BIG_EVENT, COZY_IN, UNKNOWN }

// ===================================================================
// ENUMS DA TELA "LIMITES"
// ===================================================================
enum class FidelityStyle { STRICT, FLEXIBLE, LIBERAL, UNIMPORTANT, UNKNOWN }
enum class PersonalSpaceStyle { ESSENTIAL, BALANCED, FUSED, ADAPTIVE, UNKNOWN }
enum class RelationshipGoal { PARTNERSHIP, BALANCE_TRUTH, INDIVIDUALITY, INTENSITY, UNKNOWN }

// ===================================================================
// DATA CLASS PARA O VETOR DE PERSONALIDADE
// ===================================================================
data class PersonalityVector(
    val openness: Int = 50,
    val conscientiousness: Int = 50,
    val extraversion: Int = 50,
    val agreeableness: Int = 50,
    val neuroticism: Int = 50
) {
    fun toMap(): Map<String, Any> {
        return mapOf(
            "openness" to openness,
            "conscientiousness" to conscientiousness,
            "extraversion" to extraversion,
            "agreeableness" to agreeableness,
            "neuroticism" to neuroticism
        )
    }
}

// ===================================================================
// PERFIL PSICOLÓGICO COMPLETO UNIFICADO
// ===================================================================
data class PsycheProfile(
    // Sobre Você
    val lifePace: LifePace = LifePace.INDEFINIDO,
    val socialStyle: SocialStyle = SocialStyle.INDEFINIDO,
    val planningStyle: PlanningStyle = PlanningStyle.INDEFINIDO,
    val lifeFocus: Focus = Focus.INDEFINIDO,
    val comfortZone: ComfortZone = ComfortZone.INDEFINIDO,

    // Hobbies
    val freeTimeActivity: FreeTimeActivity = FreeTimeActivity.UNKNOWN,
    val leisureType: LeisureType = LeisureType.UNKNOWN,
    val passionTopic: PassionTopic = PassionTopic.UNKNOWN,
    val idealExperience: IdealExperience = IdealExperience.UNKNOWN,
    val selfPerception: SelfPerception = SelfPerception.UNKNOWN,
    val idealDate: IdealDate = IdealDate.UNKNOWN,

    // Limites
    val fidelityStyle: FidelityStyle = FidelityStyle.UNKNOWN,
    val personalSpaceStyle: PersonalSpaceStyle = PersonalSpaceStyle.UNKNOWN,
    val relationshipGoal: RelationshipGoal = RelationshipGoal.UNKNOWN,

    // Vetor de Personalidade e Valores
    val personalityVector: PersonalityVector = PersonalityVector(),
    val coreValues: List<String> = emptyList()
) {
    fun toMap(): Map<String, Any> {
        return mapOf(
            "lifePace" to lifePace.name,
            "socialStyle" to socialStyle.name,
            "planningStyle" to planningStyle.name,
            "lifeFocus" to lifeFocus.name,
            "comfortZone" to comfortZone.name,
            "freeTimeActivity" to freeTimeActivity.name,
            "leisureType" to leisureType.name,
            "passionTopic" to passionTopic.name,
            "idealExperience" to idealExperience.name,
            "selfPerception" to selfPerception.name,
            "idealDate" to idealDate.name,
            "fidelityStyle" to fidelityStyle.name,
            "personalSpaceStyle" to personalSpaceStyle.name,
            "relationshipGoal" to relationshipGoal.name,
            "personalityVector" to personalityVector.toMap(),
            "coreValues" to coreValues
        )
    }
}
