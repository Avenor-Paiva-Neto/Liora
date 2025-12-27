package com.example.liora.domain.usecase

import com.example.liora.domain.model.*

/**
 * Um Caso de Uso especialista em traduzir as respostas brutas do questionário
 * em um perfil psicológico estruturado (PsycheProfile).
 *
 * É aqui que a "Bíblia de Regras" da IA do Liora é implementada.
 */
class MapAnswersToPsycheProfileUseCase {

    operator fun invoke(answers: Map<Int, List<String>>): PsycheProfile {

        // 1. Inicializamos o "Vetor de Personalidade" com valores neutros (50/100)
        var openness = 50
        var conscientiousness = 50
        var extraversion = 50
        var agreeableness = 50
        var neuroticism = 50

        // ===================================================================
        // ANÁLISE DA TELA "SOBRE VOCÊ" (Perguntas 1-5)
        // ===================================================================
        val lifePace = when (answers[1]?.firstOrNull()) {
            "Agitado, sempre em movimento" -> { extraversion += 10; neuroticism += 5; LifePace.AGITADO }
            "Equilibrado, entre agito e paz" -> { agreeableness += 5; neuroticism -= 10; LifePace.EQUILIBRADO }
            "Tranquilo, gosto de calmaria" -> { extraversion -= 15; LifePace.TRANQUILO }
            "Depende da fase que estou vivendo" -> { openness += 10; LifePace.ADAPTAVEL }
            else -> LifePace.INDEFINIDO
        }
        val socialStyle = when (answers[2]?.firstOrNull()) {
            "Caseira, amo ficar no meu cantinho" -> { extraversion -= 20; SocialStyle.CASEIRO }
            "Aventureira, topo quase tudo" -> { extraversion += 10; openness += 20; SocialStyle.AVENTUREIRO }
            "Social, gosto de estar cercado(a) de gente" -> { extraversion += 20; agreeableness += 5; SocialStyle.SOCIAL }
            "Independente, curto estar comigo mesmo(a)" -> { conscientiousness += 10; SocialStyle.INDEPENDENTE }
            else -> SocialStyle.INDEFINIDO
        }
        val planningStyle = when (answers[3]?.firstOrNull()) {
            "Planejo quase tudo" -> { conscientiousness += 20; PlanningStyle.PLANEJADOR }
            "Tenho uma ideia, mas deixo espaço pra imprevistos" -> { conscientiousness += 5; openness += 5; PlanningStyle.FLEXIVEL }
            "Vou vivendo, deixo fluir" -> { conscientiousness -= 15; openness += 10; PlanningStyle.FLUIDO }
            "Depende da vibe do dia" -> { openness += 5; PlanningStyle.ADAPTAVEL }
            else -> PlanningStyle.INDEFINIDO
        }
        val lifeFocus = when (answers[4]?.firstOrNull()) {
            "Trabalho muito e mal paro" -> { conscientiousness += 20; neuroticism += 5; Focus.TRABALHO }
            "Estudo bastante, mas sei curtir" -> { conscientiousness += 10; openness += 5; Focus.ESTUDO_E_EQUILIBRIO }
            "Busco equilíbrio entre trabalho e lazer" -> { agreeableness += 5; neuroticism -= 5; Focus.EQUILIBRIO }
            "Tô numa fase de transição e descobertas" -> { openness += 15; neuroticism += 5; Focus.TRANSICAO }
            else -> Focus.INDEFINIDO
        }
        val comfortZone = when (answers[5]?.firstOrNull()) {
            "Em casa, no meu espaço" -> { extraversion -= 15; conscientiousness += 5; ComfortZone.CASA }
            "Em viagens, conhecendo lugares novos" -> { openness += 20; extraversion += 10; ComfortZone.EXPLORACAO }
            "Em eventos e festas com amigos" -> { extraversion += 25; ComfortZone.SOCIAL }
            "Em contato com a natureza ou coisas simples" -> { openness += 10; agreeableness += 10; ComfortZone.NATUREZA }
            else -> ComfortZone.INDEFINIDO
        }

        // ===================================================================
        // ANÁLISE DA TELA "HOBBIES & INTERESSES" (Perguntas 6-11)
        // ===================================================================
        val freeTimeActivity = when (answers[6]?.firstOrNull()) {
            "Maratonar séries/filmes" -> { conscientiousness -= 5; neuroticism += 5; extraversion -= 5; FreeTimeActivity.MEDIA_CONSUMER }
            "Ler ou escrever" -> { openness += 20; conscientiousness += 10; extraversion -= 10; FreeTimeActivity.INTELLECTUAL }
            "Jogar (PC, console ou mobile)" -> { openness += 10; neuroticism += 5; extraversion += 5; FreeTimeActivity.GAMER }
            "Sair com amigos" -> { extraversion += 20; agreeableness += 10; FreeTimeActivity.SOCIAL }
            "Criar algo (desenhar, editar, compor, etc.)" -> { openness += 25; conscientiousness += 5; extraversion -= 5; FreeTimeActivity.CREATOR }
            "Outra vibe (vou contar no final do perfil)" -> { openness += 15; neuroticism += 5; FreeTimeActivity.OTHER }
            else -> FreeTimeActivity.UNKNOWN
        }
        val leisureType = when (answers[7]?.firstOrNull()) {
            "Cultura (museus, shows, eventos)" -> { openness += 15; extraversion += 5; LeisureType.CULTURE }
            "Esporte ou academia" -> { conscientiousness += 10; extraversion += 10; LeisureType.SPORTS }
            "Natureza e viagens" -> { openness += 20; agreeableness += 5; LeisureType.NATURE_TRAVEL }
            "Festas e baladas" -> { extraversion += 25; conscientiousness -= 5; LeisureType.PARTY }
            "Ficar de boa, relaxando em casa" -> { extraversion -= 10; conscientiousness += 10; neuroticism -= 5; LeisureType.RELAX }
            else -> LeisureType.UNKNOWN
        }
        val passionTopic = when (answers[8]?.firstOrNull()) {
            "Música" -> { openness += 15; agreeableness += 5; PassionTopic.MUSIC }
            "Filmes e séries" -> { openness += 10; neuroticism += 5; PassionTopic.MEDIA }
            "Games" -> { openness += 10; extraversion += 5; neuroticism += 5; PassionTopic.GAMES }
            "Livros e filosofia" -> { openness += 25; extraversion -= 10; conscientiousness += 5; PassionTopic.PHILOSOPHY }
            "Tecnologia e futuro" -> { openness += 20; conscientiousness += 5; PassionTopic.TECHNOLOGY }
            "Memes e cultura pop" -> { extraversion += 10; agreeableness += 5; openness += 5; PassionTopic.POP_CULTURE }
            else -> PassionTopic.UNKNOWN
        }
        val idealExperience = when (answers[9]?.firstOrNull()) {
            "Viajar pra um lugar inusitado" -> { openness += 20; extraversion += 10; IdealExperience.UNUSUAL_TRAVEL }
            "Cozinhar algo juntos" -> { agreeableness += 15; conscientiousness += 10; IdealExperience.COOKING }
            "Ter noites de jogos ou maratona" -> { agreeableness += 10; openness += 5; neuroticism += 5; IdealExperience.GAME_NIGHT }
            "Fazer um projeto criativo juntos" -> { openness += 25; agreeableness += 10; conscientiousness += 10; IdealExperience.CREATIVE_PROJECT }
            "Ficar em silêncio confortável só curtindo" -> { agreeableness += 15; neuroticism -= 5; extraversion -= 5; IdealExperience.QUIET_COMFORT }
            else -> IdealExperience.UNKNOWN
        }
        val selfPerception = when (answers[10]?.firstOrNull()) {
            "Minha mente nunca para" -> { openness += 15; neuroticism += 10; SelfPerception.RESTLESS_MIND }
            "Eu vivo colecionando momentos" -> { openness += 10; agreeableness += 5; SelfPerception.MOMENT_COLLECTOR }
            "Me perco fácil em boas histórias" -> { openness += 20; conscientiousness -= 5; SelfPerception.STORY_LOVER }
            "Gosto de rir até a barriga doer" -> { extraversion += 20; agreeableness += 10; SelfPerception.HUMOROUS }
            "Curto viver no meu próprio ritmo" -> { openness += 15; conscientiousness += 5; extraversion -= 5; SelfPerception.OWN_RHYTHM }
            else -> SelfPerception.UNKNOWN
        }
        val idealDate = when (answers[11]?.firstOrNull()) {
            "Algo simples e real, tipo um café e boa conversa" -> { agreeableness += 10; conscientiousness += 5; neuroticism -= 5; IdealDate.SIMPLE_CONVERSATION }
            "Um rolê diferente, tipo karaokê, escape room…" -> { openness += 20; extraversion += 10; IdealDate.UNIQUE_ACTIVITY }
            "Ir pra um evento, show ou festa" -> { extraversion += 25; conscientiousness -= 5; IdealDate.BIG_EVENT }
            "Um dia mais off, filme, coberta e risada" -> { agreeableness += 10; neuroticism += 5; extraversion -= 5; IdealDate.COZY_IN }
            else -> IdealDate.UNKNOWN
        }

        // ===================================================================
        // ANÁLISE DA TELA "PERSONALIDADE" (Perguntas 12-16)
        // ===================================================================
        when (answers[12]?.firstOrNull()) { "Sim, sou bem transparente" -> { extraversion += 15; agreeableness += 10 }; "Depende da conexão" -> { openness += 5; agreeableness += 5 }; "Não muito, levo tempo" -> { extraversion -= 10; conscientiousness += 5 }; "Raramente, sou bem reservado(a)" -> { extraversion -= 15; neuroticism += 10 } }
        when (answers[13]?.firstOrNull()) { "Prefiro resolver na hora, com diálogo" -> { agreeableness += 15; conscientiousness += 10 }; "Me afasto um pouco e penso antes" -> { conscientiousness += 10; neuroticism -= 5 }; "Fico na minha, evito conflito ao máximo" -> { extraversion -= 10; neuroticism += 10 }; "Depende muito da pessoa e da situação" -> { openness += 10; agreeableness += 5 } }
        when (answers[14]?.firstOrNull()) { "Sarcástico/irônico" -> { openness += 15; agreeableness -= 10 }; "Bobo e leve" -> { agreeableness += 10; extraversion += 5 }; "Inteligente e provocador" -> { openness += 10; extraversion += 5 }; "Tímido, mas presente" -> { extraversion -= 5; agreeableness += 5 }; "Não sou muito de piadas" -> { extraversion -= 10; conscientiousness += 5 } }
        when (answers[15]?.firstOrNull()) { "Demonstrar sem medo" -> { extraversion += 15; neuroticism -= 5 }; "Mostrar aos poucos" -> { conscientiousness += 5; agreeableness += 5 }; "Esperar sinais da outra pessoa" -> { extraversion -= 5; neuroticism += 10 }; "Guardar pra mim" -> { extraversion -= 15; neuroticism += 15 } }
        when (answers[16]?.firstOrNull()) { "Sinto tudo com muita intensidade" -> { neuroticism += 20; openness += 10 }; "Eu observo mais do que falo" -> { extraversion -= 10; conscientiousness += 5 }; "Sou do tipo coração aberto" -> { agreeableness += 15; extraversion += 10 }; "Sou mais razão que emoção" -> { conscientiousness += 10; neuroticism -= 10 }; "Tenho meu próprio jeito de sentir" -> { openness += 10; conscientiousness += 5 } }

        // ===================================================================
        // ANÁLISE DA TELA "LIMITES & VALORES" (Perguntas 17-20)
        // ===================================================================
        val coreValues = answers[17] ?: emptyList()
        val fidelityStyle = when (answers[18]?.firstOrNull()) { "Sim, totalmente" -> { agreeableness += 15; conscientiousness += 10; FidelityStyle.STRICT }; "Sim, mas depende do tipo de relação" -> { openness += 10; conscientiousness += 5; FidelityStyle.FLEXIBLE }; "Acredito em liberdade com respeito" -> { openness += 15; agreeableness += 5; FidelityStyle.LIBERAL }; "Não é algo essencial pra mim" -> { openness += 20; conscientiousness -= 10; FidelityStyle.UNIMPORTANT }; else -> FidelityStyle.UNKNOWN }
        val personalSpaceStyle = when (answers[19]?.firstOrNull()) { "Acho essencial, cada um precisa do seu" -> { conscientiousness += 10; openness += 10; neuroticism -= 5; PersonalSpaceStyle.ESSENTIAL }; "Gosto de estar junto, mas entendo o espaço" -> { agreeableness += 15; conscientiousness += 5; PersonalSpaceStyle.BALANCED }; "Prefiro ficar sempre perto" -> { neuroticism += 15; openness -= 5; agreeableness += 10; PersonalSpaceStyle.FUSED }; "Depende do tipo de conexão" -> { openness += 10; conscientiousness += 5; PersonalSpaceStyle.ADAPTIVE }; else -> PersonalSpaceStyle.UNKNOWN }
        val relationshipGoal = when (answers[20]?.firstOrNull()) { "Quero um parceiro(a) que some e não sufoque" -> { openness += 10; conscientiousness += 10; RelationshipGoal.PARTNERSHIP }; "Quero construir algo com equilíbrio e verdade" -> { conscientiousness += 15; agreeableness += 10; RelationshipGoal.BALANCE_TRUTH }; "Busco alguém que entenda minhas individualidades" -> { openness += 20; agreeableness += 5; RelationshipGoal.INDIVIDUALITY }; "Procuro intensidade e entrega total" -> { neuroticism += 20; agreeableness += 10; conscientiousness -= 5; RelationshipGoal.INTENSITY }; else -> RelationshipGoal.UNKNOWN }

        // Finalizamos e normalizamos os scores
        val finalPersonalityVector = PersonalityVector(
            openness = clampScore(openness),
            conscientiousness = clampScore(conscientiousness),
            extraversion = clampScore(extraversion),
            agreeableness = clampScore(agreeableness),
            neuroticism = clampScore(neuroticism)
        )

        // Retornamos o dossiê psicológico completo
        return PsycheProfile(
            lifePace = lifePace, socialStyle = socialStyle, planningStyle = planningStyle,
            lifeFocus = lifeFocus, comfortZone = comfortZone, freeTimeActivity = freeTimeActivity,
            leisureType = leisureType, passionTopic = passionTopic, idealExperience = idealExperience,
            selfPerception = selfPerception, idealDate = idealDate,
            fidelityStyle = fidelityStyle, personalSpaceStyle = personalSpaceStyle,
            relationshipGoal = relationshipGoal, personalityVector = finalPersonalityVector,
            coreValues = coreValues
        )
    }

    private fun clampScore(score: Int): Int = score.coerceIn(0, 100)
}