package com.example.liora.domain.usecase

import com.example.liora.domain.model.ImageDisplayInfo
import com.example.liora.domain.model.UserProfile
import com.example.liora.domain.repository.ChatRepository
import java.util.concurrent.TimeUnit // Importe TimeUnit para conversão de tempo

/**
 * Use Case para determinar se a imagem de perfil de um usuário deve ser borrada
 * com base no timestamp da primeira mensagem em um chat e um período de tempo definido.
 *
 * Este use case é agnóstico à fonte de dados e à implementação da UI, focando
 * puramente na regra de negócio de revelação da imagem.
 *
 * @param chatRepository Repositório para acessar o timestamp da primeira mensagem do chat.
 * Ele é injetado, seguindo o Princípio da Inversão de Dependência.
 */
class DetermineImageBlurStatusUseCase(
    private val chatRepository: ChatRepository
) {
    // Constante para o período de tempo após o qual a imagem é revelada (17 horas em milissegundos).
    // Definida como constante para facilitar a manutenção e legibilidade.
    private val REVEAL_DELAY_MS = TimeUnit.HOURS.toMillis(17)

    /**
     * Invoca o Use Case para obter as informações de exibição da imagem para um dado perfil
     * e chat.
     *
     * @param userProfile O [UserProfile] do usuário cuja imagem será avaliada. Contém a URL da imagem.
     * @param chatId O ID do chat associado ao match entre o usuário logado e este [userProfile].
     * Este `chatId` é crucial para buscar o timestamp da primeira mensagem no `ChatRepository`.
     * @return Um [ImageDisplayInfo] contendo a URL da imagem e um booleano indicando
     * se a imagem deve ser borrada (`true`) ou exibida claramente (`false`).
     */
    suspend operator fun invoke(userProfile: UserProfile, chatId: String): ImageDisplayInfo {
        // Tenta obter a primeira URL de imagem do perfil. Se não houver, será nula.
        val imageUrl = userProfile.imageUrls.firstOrNull()

        // Se não houver URL de imagem válida, não há o que exibir.
        // Retornamos um ImageDisplayInfo com URL nula e `shouldBlur = true`
        // para garantir que nada seja exibido ou que um placeholder borrado possa ser usado.
        if (imageUrl.isNullOrEmpty()) {
            return ImageDisplayInfo(null, true)
        }

        // Tenta obter o timestamp da primeira mensagem do chat usando o ChatRepository.
        // O `getOrNull()` é usado para lidar com o Result, retornando null em caso de falha.
        val result = chatRepository.getFirstMessageTimestamp(chatId)
        val firstMessageTimestamp = result.getOrNull()

        // Lógica para determinar se a imagem deve ser borrada:
        val shouldBlur = if (firstMessageTimestamp == null) {
            // Caso 1: Não há timestamp da primeira mensagem.
            // Isso pode acontecer se o chat ainda não foi iniciado (sem mensagens).
            // Nesses casos, a imagem deve permanecer borrada.
            true
        } else {
            // Caso 2: Há um timestamp da primeira mensagem.
            // Calcula o tempo decorrido desde a primeira mensagem até o momento atual.
            val currentTime = System.currentTimeMillis()
            val elapsedTime = currentTime - firstMessageTimestamp

            // A imagem deve ser borrada se o tempo decorrido for MENOR que o atraso de revelação.
            // Caso contrário, (tempo decorrido >= REVEAL_DELAY_MS), a imagem não deve ser borrada.
            elapsedTime < REVEAL_DELAY_MS
        }

        // Retorna o objeto ImageDisplayInfo com a URL da imagem e o status de blur determinado.
        return ImageDisplayInfo(imageUrl, shouldBlur)
    }
}