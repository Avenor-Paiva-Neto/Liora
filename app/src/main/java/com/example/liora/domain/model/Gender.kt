package com.example.liora.domain.model

/**
 * Representa o gênero inferido de um usuário.
 * Usar um enum garante que só podemos ter estes valores,
 * tornando o código mais seguro e legível.
 */
enum class Gender {
    HOMEM,
    MULHER,
    NAO_BINARIO, // Para futuras expansões
    INDEFINIDO   // Um valor padrão caso a inferência não seja possível
}