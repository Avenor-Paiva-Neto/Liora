package com.example.liora.domain.usecase

import com.example.liora.domain.repository.ChatRepository

/**
 * Use Case para garantir que o documento principal de um chat exista no Firestore
 * com os IDs dos participantes corretos.
 *
 * Esta classe faz parte da camada de domínio da Clean Architecture, encapsulando
 * uma regra de negócio específica: a inicialização ou verificação do documento
 * pai de um chat. Ela depende da abstração [ChatRepository] para interagir
 * com a camada de dados.
 *
 * @param chatRepository Uma instância da implementação de [ChatRepository] para acessar
 * operações de persistência de chat.
 */
class EnsureChatDocumentUseCase(
    private val chatRepository: ChatRepository
) {
    /**
     * Operador `invoke` que permite que a instância do Use Case seja chamada como uma função.
     *
     * @param chatId O identificador único da conversa.
     * @param user1Id O UID do primeiro participante do chat.
     * @param user2Id O UID do segundo participante do chat.
     * @return [Result]<Unit> Indicando sucesso ou falha da operação de garantia do documento.
     */
    suspend operator fun invoke(chatId: String, user1Id: String, user2Id: String): Result<Unit> {
        return chatRepository.ensureChatDocumentExists(chatId, user1Id, user2Id)
    }
}