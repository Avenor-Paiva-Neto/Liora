package com.example.liora.domain.model

/**
 * Representa a estrutura de dados de uma única mensagem dentro do sistema de chat.
 *
 * Esta data class encapsula todos os atributos essenciais de uma mensagem,
 * garantindo a integridade e a tipagem forte dos dados que transitam entre
 * as camadas da aplicação (UI, Domain, Data).
 *
 * Como parte da camada de domínio, Message.kt é uma entidade pura. Ela não contém
 * lógica de negócios complexa nem dependências de frameworks Android ou bibliotecas
 * de persistência, aderindo ao princípio da Independência de Frameworks e ao
 * Princípio da Responsabilidade Única (SRP).
 *
 * @property id Identificador único da mensagem. Essencial para referenciar a mensagem
 * no banco de dados e para operações como atualização de status.
 * @property senderId O UID do usuário que enviou a mensagem. Permite identificar o
 * remetente e diferenciar mensagens próprias das mensagens do interlocutor na UI.
 * @property content O conteúdo textual da mensagem. Em futuras expansões, poderia ser
 * o URL de uma imagem ou vídeo.
 * @property timestamp O momento em que a mensagem foi enviada, representado em
 * milissegundos desde a Época (Unix timestamp). Crucial para ordenar as mensagens
 * cronologicamente.
 * @property type Indica o tipo de conteúdo da mensagem (ex: "text", "image", "video").
 * Permite a flexibilidade para suportar diferentes tipos de mídia no futuro.
 * @property status Representa o estado atual da mensagem (ex: "sent", "delivered", "seen").
 * Usado para feedback visual ao usuário.
 */
data class Message(
    val id: String = "",         // Adicione um valor padrão
    val senderId: String = "",   // Adicione um valor padrão
    val content: String = "",    // Adicione um valor padrão
    val timestamp: Long = 0L,    // Adicione um valor padrão
    val type: String = "text",
    val status: String = "sent"
)
