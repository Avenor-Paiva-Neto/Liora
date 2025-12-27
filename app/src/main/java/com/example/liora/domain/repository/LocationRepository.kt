package com.example.liora.domain.repository

/**
 * Contrato para o repositório que lida com dados de localização.
 */
interface LocationRepository {

    /**
     * Busca a localização atual do usuário e a retorna como uma String formatada "Cidade, Estado".
     * Retorna um Result que encapsula o sucesso ou a falha da operação.
     */
    suspend fun getCurrentCityState(): Result<String>
}