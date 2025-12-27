package com.example.liora.ui.conversations

import com.example.liora.domain.model.ImageDisplayInfo
import com.example.liora.domain.model.UserProfile

/**
 * Representa um único item na lista de conversas da UI.
 *
 * Esta data class combina o [UserProfile] de um match com as informações
 * de como a imagem de perfil desse match deve ser exibida ([ImageDisplayInfo]),
 * incluindo se ela deve ser borrada.
 *
 * Isso facilita a passagem de todos os dados relevantes para a UI de forma coesa.
 *
 * @property userProfile O perfil completo do usuário com quem há um match.
 * @property imageDisplayInfo As informações sobre a exibição da imagem de perfil,
 * incluindo a URL e o status de blur.
 */
data class ConversationListItem(
    val userProfile: UserProfile,
    val imageDisplayInfo: ImageDisplayInfo
)