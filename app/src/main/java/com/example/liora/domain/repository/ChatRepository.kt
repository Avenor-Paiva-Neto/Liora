package com.example.liora.domain.repository

import com.example.liora.domain.model.Message
import kotlinx.coroutines.flow.Flow

/**
 * Interface que define o contrato para as operações de persistência e recuperação
 * de mensagens de chat.
 *
 * No contexto da Clean Architecture, esta interface atua como uma abstração
 * que permite que os Use Cases na camada de domínio interajam com os dados
 * de chat sem se preocupar com os detalhes de implementação de como esses
 * dados são armazenados ou recuperados (por exemplo, se é Firebase, um banco de dados
 * local, ou outro serviço).
 *
 * Adere ao Princípio da Inversão de Dependência (DIP) do SOLID.
 */
interface ChatRepository {

    /**
     * Envia uma nova mensagem para uma conversa específica.
     *
     * Esta é uma função de suspensão, adequada para operações assíncronas
     * como o envio de dados para um banco de dados remoto.
     *
     * @param matchId O identificador único da conversa (sala de chat).
     * @param message O objeto [Message] a ser enviado, contendo todos os seus detalhes.
     * @return [Result]<Unit> Indicando sucesso ou falha da operação.
     */
    suspend fun sendMessage(matchId: String, message: Message): Result<Unit>

    /**
     * Fornece um fluxo reativo de mensagens para uma conversa específica em tempo real.
     *
     * Esta função é crucial para a funcionalidade de chat em tempo real, onde novas
     * mensagens devem aparecer instantaneamente na UI. O [Flow] emitirá uma nova
     * lista de mensagens sempre que houver uma alteração na fonte de dados.
     *
     * @param matchId O identificador único da conversa a ser observada.
     * @return [Flow]<List<Message>> Um fluxo contínuo da lista de mensagens da conversa.
     */
    fun observeMessages(matchId: String): Flow<List<Message>>

    /**
     * Atualiza o status de uma mensagem específica dentro de uma conversa.
     *
     * Usado para fornecer feedback visual ao usuário sobre o estado da mensagem
     * (ex: de "sent" para "delivered" ou "seen").
     *
     * @param matchId O identificador da conversa onde a mensagem está localizada.
     * @param messageId O identificador único da mensagem cujo status será atualizado.
     * @param status O novo status a ser atribuído à mensagem (ex: "sent", "delivered", "seen").
     * @return [Result]<Unit> Indicando sucesso ou falha da operação.
     */
    suspend fun updateMessageStatus(matchId: String, messageId: String, status: String): Result<Unit>

    /**
     * Garante que o documento principal do chat exista no Firestore com os IDs dos participantes.
     * Se o documento não existir, ele é criado. Se existir, nenhuma operação é realizada.
     *
     * @param chatId O identificador único da conversa.
     * @param user1Id O UID do primeiro participante do chat.
     * @param user2Id O UID do segundo participante do chat.
     * @return [Result]<Unit> Indicando sucesso ou falha da operação.
     */
    suspend fun ensureChatDocumentExists(chatId: String, user1Id: String, user2Id: String): Result<Unit>

    /**
     * NOVO MÉTODO: Obtém o timestamp da primeira mensagem enviada em um chat específico.
     *
     * Este timestamp é usado para determinar quando a imagem de perfil de um match
     * deve ser revelada (sem blur).
     *
     * @param chatId O identificador único da conversa.
     * @return [Result]<Long?> Um Result contendo o timestamp da primeira mensagem em milissegundos,
     * ou null se o chat não tiver mensagens ou o campo não estiver definido.
     */
    suspend fun getFirstMessageTimestamp(chatId: String): Result<Long?>
}