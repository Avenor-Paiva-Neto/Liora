package com.example.liora.domain.repository

import android.net.Uri

/**
 * Interface (Contrato) que define as operações para arquivos, como imagens de perfil.
 */
interface StorageRepository {

    /**
     * Faz o upload de uma imagem de perfil, processando-a antes de enviar.
     * @param userId O ID do usuário ao qual a imagem pertence, para organizar o armazenamento.
     * @param imageUri A URI local da imagem no dispositivo do usuário.
     * @return Um Result contendo a URL de download pública da imagem em caso de sucesso.
     */
    suspend fun uploadProfileImage(userId: String, imageUri: Uri): Result<String>
}