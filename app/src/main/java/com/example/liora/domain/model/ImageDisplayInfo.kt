package com.example.liora.domain.model

/**
 * Representa as informações necessárias para exibir uma imagem de perfil,
 * incluindo a URL da imagem e um indicador se ela deve ser borrada.
 *
 * Esta data class é uma entidade pura da camada de domínio, não contendo lógica
 * de negócios complexa nem dependências de frameworks Android ou bibliotecas
 * de persistência. Ela serve para transmitir o estado da imagem de forma clara
 * e concisa entre as camadas da aplicação (especialmente do Use Case para a UI).
 *
 * @property imageUrl A URL (String) da imagem a ser carregada e exibida. Pode ser nula
 * se o perfil não tiver uma imagem.
 * @property shouldBlur Um valor booleano que indica se a imagem deve ter um efeito de blur
 * aplicado no momento da exibição na interface do usuário. 'true' para borrado, 'false' para claro.
 */
data class ImageDisplayInfo(
    val imageUrl: String?, // URL da imagem a ser carregada
    val shouldBlur: Boolean // Indica se o blur deve ser aplicado
)