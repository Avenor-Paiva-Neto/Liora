package com.example.liora.domain.compatibility

import com.example.liora.domain.model.Gender

/**
 * Um objeto especialista em inferir o gênero de um usuário
 * com base em sua orientação sexual e preferência de busca.
 *
 * Usamos um 'object' porque esta é uma classe de utilidade sem estado,
 * o que a torna um singleton eficiente.
 */
object GenderInferenceEngine {

    /**
     * Infere o gênero de um perfil com base em suas declarações.
     * @param sexuality A orientação sexual declarada (ex: "Hetero", "Homo", "Bissexual").
     * @param lookingFor O gênero que a pessoa procura (ex: "Homen", "Mulher").
     * @return O [Gender] inferido.
     */
    fun infer(sexuality: String, lookingFor: String): Gender {
        return when {
            // Casos heterossexuais (mais diretos)
            sexuality == "Hetero" && lookingFor == "Mulher" -> Gender.HOMEM
            sexuality == "Hetero" && lookingFor == "Homen" -> Gender.MULHER

            // Casos homossexuais
            sexuality == "Homo" && lookingFor == "Homen" -> Gender.HOMEM
            sexuality == "Lesbica" && lookingFor == "Mulher" -> Gender.MULHER

            // Para pessoas bissexuais, podemos inferir o gênero se a preferência de busca for exclusiva.
            // Ex: Uma pessoa que se identifica como Bissexual mas no momento só procura por "Homen",
            // é muito provável que se identifique como mulher.
            sexuality == "Bissexual" && lookingFor == "Homen" -> Gender.MULHER
            sexuality == "Bissexual" && lookingFor == "Mulher" -> Gender.HOMEM

            // Para qualquer outra combinação (ex: Bissexual procurando por "Outros", etc.),
            // a inferência é incerta, então retornamos INDEFINIDO por segurança.
            else -> Gender.INDEFINIDO
        }
    }
}