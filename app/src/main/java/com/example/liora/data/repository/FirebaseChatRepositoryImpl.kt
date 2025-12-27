package com.example.liora.data.repository

import com.example.liora.domain.model.Message
import com.example.liora.domain.repository.ChatRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions // Importe SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/**
 * Implementação concreta da interface [ChatRepository] que interage com o Firebase Firestore.
 *
 * Esta classe é responsável por:
 * - Enviar novas mensagens para o Firestore.
 * - Observar mensagens em tempo real usando listeners de snapshot.
 * - Atualizar o status de mensagens existentes no Firestore.
 * - Garantir a existência e dados essenciais do documento de chat principal.
 * - NOVO: Registrar e obter o timestamp da primeira mensagem enviada em um chat.
 *
 * Adere ao Princípio da Responsabilidade Única (SRP) e ao Princípio do Aberto/Fechado (OCP).
 *
 * @param firestore Instância do FirebaseFirestore injetada para comunicação com o banco de dados.
 */
class FirebaseChatRepositoryImpl(
    private val firestore: FirebaseFirestore
) : ChatRepository {

    /**
     * Envia uma nova mensagem para uma conversa específica no Firestore.
     * A mensagem é armazenada como um documento em uma subcoleção "messages"
     * dentro do documento da conversa ("chats/{matchId}").
     *
     * **MODIFICADO:** Adiciona lógica para registrar o `firstMessageTimestamp`
     * no documento principal do chat se esta for a primeira mensagem.
     *
     * @param matchId O identificador único da conversa (sala de chat).
     * @param message O objeto [Message] a ser enviado.
     * @return [Result]<Unit> Indicando sucesso ou falha da operação.
     */
    override suspend fun sendMessage(matchId: String, message: Message): Result<Unit> {
        return try {
            val chatDocRef = firestore.collection("chats").document(matchId)
            val messagesCollectionRef = chatDocRef.collection("messages")

            // Adiciona a mensagem à subcoleção
            messagesCollectionRef.document(message.id)
                .set(message)
                .await()

            // Verifica se é a primeira mensagem para registrar o timestamp no documento principal do chat.
            // Tentamos obter o documento do chat para verificar se 'firstMessageTimestamp' já existe.
            val chatDocSnapshot = chatDocRef.get().await()
            if (!chatDocSnapshot.contains("firstMessageTimestamp")) {
                // Se o campo não existe, significa que esta é a primeira mensagem,
                // então o campo é criado com o timestamp da mensagem.
                chatDocRef.update("firstMessageTimestamp", message.timestamp).await()
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e) // Retorna um Result.failure em caso de exceção
        }
    }

    /**
     * Fornece um fluxo reativo de mensagens para uma conversa específica em tempo real.
     * Utiliza `callbackFlow` para adaptar o `addSnapshotListener` do Firestore a um Kotlin Flow.
     * As mensagens são ordenadas por timestamp em ordem ascendente.
     *
     * @param matchId O identificador único da conversa a ser observada.
     * @return [Flow]<List<Message>> Um fluxo contínuo da lista de mensagens da conversa.
     */
    override fun observeMessages(matchId: String): Flow<List<Message>> = callbackFlow {
        // Referência à subcoleção de mensagens da conversa, ordenada por timestamp
        val messagesCollection = firestore.collection("chats").document(matchId)
            .collection("messages")
            .orderBy("timestamp", Query.Direction.ASCENDING) // Ordenar por timestamp para exibição cronológica

        // Adiciona o listener de snapshot para receber atualizações em tempo real
        val subscription = messagesCollection.addSnapshotListener { snapshot, e ->
            if (e != null) {
                close(e) // Envia o erro para o Flow e fecha-o
                return@addSnapshotListener
            }

            if (snapshot != null) {
                // Mapeia os documentos do snapshot para objetos Message
                val messages = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(Message::class.java)
                }
                trySend(messages) // Envia a lista de mensagens atualizada para o Flow
            } else {
                trySend(emptyList()) // Envia uma lista vazia se o snapshot for nulo (ex: conversa nova)
            }
        }

        // Garante que o listener seja removido quando o Flow é cancelado (ex: ViewModel destruído)
        awaitClose { subscription.remove() }
    }

    /**
     * Atualiza o status de uma mensagem específica dentro de uma conversa no Firestore.
     *
     * @param matchId O identificador da conversa onde a mensagem está localizada.
     * @param messageId O identificador único da mensagem cujo status será atualizado.
     * @param status O novo status a ser atribuído à mensagem (ex: "seen").
     * @return [Result]<Unit> Indicando sucesso ou falha da operação.
     */
    override suspend fun updateMessageStatus(matchId: String, messageId: String, status: String): Result<Unit> {
        return try {
            // Atualiza o campo 'status' do documento da mensagem
            firestore.collection("chats").document(matchId)
                .collection("messages").document(messageId)
                .update("status", status) // Atualiza apenas o campo 'status'
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Garante que o documento principal do chat exista no Firestore com os IDs dos participantes.
     * Se o documento não existir, ele é criado. Se existir, nenhuma operação é realizada,
     * respeitando a regra `allow update: if false` para o documento do chat.
     *
     * @param chatId O identificador único da conversa.
     * @param user1Id O UID do primeiro participante do chat.
     * @param user2Id O UID do segundo participante do chat.
     * @return [Result]<Unit> Indicando sucesso ou falha da operação.
     */
    override suspend fun ensureChatDocumentExists(chatId: String, user1Id: String, user2Id: String): Result<Unit> {
        return try {
            val chatDocumentRef = firestore.collection("chats").document(chatId)

            // Tenta obter o documento primeiro
            val snapshot = chatDocumentRef.get().await()

            // Se o documento não existe, nós o criamos
            if (!snapshot.exists()) {
                val chatData = hashMapOf(
                    "user1Id" to user1Id,
                    "user2Id" to user2Id,
                    "createdAt" to System.currentTimeMillis() // Opcional: adicionar timestamp de criação
                )
                // Apenas cria o documento se ele não existir
                chatDocumentRef.set(chatData).await() // Não usamos merge aqui, é uma criação inicial
            }
            // Se o documento já existe, não fazemos nada, pois user1Id e user2Id já devem estar presentes
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * NOVO MÉTODO: Obtém o timestamp da primeira mensagem enviada em um chat específico.
     *
     * Busca o campo `firstMessageTimestamp` do documento principal do chat.
     *
     * @param chatId O identificador único da conversa.
     * @return [Result]<Long?> Um Result contendo o timestamp da primeira mensagem em milissegundos,
     * ou null se o chat não tiver mensagens ou o campo não estiver definido.
     */
    override suspend fun getFirstMessageTimestamp(chatId: String): Result<Long?> {
        return try {
            val chatDocRef = firestore.collection("chats").document(chatId)
            val snapshot = chatDocRef.get().await()

            if (snapshot.exists()) {
                // Tenta ler o campo 'firstMessageTimestamp'. getLong() retorna null se o campo não existir.
                val timestamp = snapshot.getLong("firstMessageTimestamp")
                Result.success(timestamp) // Retorna o timestamp ou null
            } else {
                // Se o documento do chat não existe, não há timestamp de primeira mensagem para ele.
                Result.success(null)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}