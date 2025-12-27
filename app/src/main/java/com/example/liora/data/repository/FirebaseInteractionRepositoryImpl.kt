package com.example.liora.data.repository

import com.example.liora.domain.model.UserProfile
import com.example.liora.domain.repository.InteractionRepository
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.Collections

/**
 * Implementação concreta do InteractionRepository que utiliza o Google Firestore como backend.
 *
 * Responsável por todas as operações de leitura e escrita na coleção 'interactions',
 * bem como as buscas relacionadas na coleção 'users'.
 */
class FirebaseInteractionRepositoryImpl(
    private val firestore: FirebaseFirestore
) : InteractionRepository {

    // Referências para as coleções que serão utilizadas.
    private val interactionsCollection = firestore.collection("interactions")
    private val usersCollection = firestore.collection("users")
    private val matchesCollection = firestore.collection("matches") // NOVA COLEÇÃO PARA MATCHES

    /**
     * Registra uma interação (like/dislike) de um usuário para outro no Firestore.
     */
    override suspend fun recordInteraction(fromUserId: String, toUserId: String, action: String): Result<Unit> {
        return try {
            val documentId = "${fromUserId}_${toUserId}"
            val interactionData = mapOf(
                "fromUserId" to fromUserId,
                "toUserId" to toUserId,
                "action" to action,
                "timestamp" to FieldValue.serverTimestamp()
            )
            interactionsCollection.document(documentId).set(interactionData).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Verifica se existe um match (curtida mútua) entre dois usuários.
     *
     * IMPORTANTE: Esta função agora APENAS VERIFICA o match.
     * A persistência do documento na coleção 'matches' DEVE ser feita
     * no Use Case (ProcessLikeActionUseCase) que chama esta função,
     * APÓS a confirmação de um match mútuo, chamando o método createMatch.
     */
    override suspend fun checkForMatch(userA_id: String, userB_id: String): Result<Boolean> {
        return try {
            val interactionAtoB_id = "${userA_id}_${userB_id}"
            val interactionBtoA_id = "${userB_id}_${userA_id}"

            val docAtoB = interactionsCollection.document(interactionAtoB_id).get().await()
            val docBtoA = interactionsCollection.document(interactionBtoA_id).get().await()

            val aLikedB = docAtoB.exists() && docAtoB.getString("action") == "like"
            val bLikedA = docBtoA.exists() && docBtoA.getString("action") == "like"

            Result.success(aLikedB && bLikedA)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Obtém uma lista de todos os IDs de usuários com os quais o usuário atual já interagiu.
     */
    override suspend fun getPreviouslyInteractedUserIds(currentUserId: String): Result<List<String>> {
        return try {
            val querySnapshot = interactionsCollection
                .whereEqualTo("fromUserId", currentUserId)
                .get()
                .await()

            val seenIds = querySnapshot.documents.mapNotNull { it.getString("toUserId") }

            Result.success(seenIds)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Busca os perfis completos de todos os usuários que deram 'like' no usuário atual.
     * CORREÇÃO: Chunk size ajustado para 10 para compatibilidade segura com 'whereIn'.
     */
    override suspend fun getLikerProfiles(currentUserId: String): Result<List<UserProfile>> {
        return try {
            val likerIds = interactionsCollection
                .whereEqualTo("toUserId", currentUserId)
                .whereEqualTo("action", "like")
                .get()
                .await()
                .documents.mapNotNull { it.getString("fromUserId") }

            if (likerIds.isEmpty()) {
                return Result.success(emptyList())
            }

            val profiles = mutableListOf<UserProfile>()
            // Ajustado para chunked(10) para respeitar o limite do Firestore para 'whereIn'
            likerIds.chunked(10).forEach { idChunk ->
                val profilesChunk = usersCollection
                    .whereIn("uid", idChunk)
                    .get()
                    .await()
                    .toObjects(UserProfile::class.java)
                profiles.addAll(profilesChunk)
            }

            Result.success(profiles)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Deleta um documento de interação específico.
     *
     * Essencial para a função "desfazer", para que o perfil pulado possa
     * ser considerado novamente pelo sistema de match.
     */
    override suspend fun deleteInteraction(fromUserId: String, toUserId: String): Result<Unit> {
        return try {
            // Reconstrói o ID do documento da mesma forma que ele foi criado em 'recordInteraction'.
            val documentId = "${fromUserId}_${toUserId}"

            // Executa a operação de delete no documento correspondente.
            interactionsCollection.document(documentId).delete().await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Observa os IDs dos usuários com quem o usuário atual teve um match mútuo.
     *
     * Escuta em tempo real a nova coleção 'matches', buscando documentos onde o usuário atual
     * é um dos participantes (user1Id ou user2Id) e retorna o ID do outro participante
     * E O ID DO CHAT (que é o matchDocumentId).
     *
     * @param currentUserId O ID do usuário logado.
     * @return Um [Flow] de uma lista de [Pair]s, onde cada Pair contém o UID do usuário
     * com match e o ID do chat correspondente.
     */
    override fun observeMutualMatches(currentUserId: String): Flow<List<Pair<String, String>>> = callbackFlow {
        val listenerRegistration = matchesCollection
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    close(e)
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val matchedUsersAndChatIds = mutableListOf<Pair<String, String>>()
                    for (doc in snapshot.documents) {
                        val user1Id = doc.getString("user1Id")
                        val user2Id = doc.getString("user2Id")
                        val matchDocumentId = doc.id // O ID do documento de match é o chatId!

                        if (user1Id == currentUserId && user2Id != null) {
                            matchedUsersAndChatIds.add(Pair(user2Id, matchDocumentId))
                        } else if (user2Id == currentUserId && user1Id != null) {
                            matchedUsersAndChatIds.add(Pair(user1Id, matchDocumentId))
                        }
                    }
                    trySend(matchedUsersAndChatIds.toList())
                } else {
                    trySend(emptyList())
                }
            }

        awaitClose { listenerRegistration.remove() }
    }

    /**
     * IMPLEMENTAÇÃO NOVA: Registra um match mútuo entre dois usuários na coleção de matches.
     *
     * O ID do documento é gerado a partir dos UIDs dos dois usuários em ordem alfabética
     * para garantir unicidade e fácil consulta.
     */
    override suspend fun createMatch(user1Id: String, user2Id: String): Result<Unit> {
        return try {
            // Garante que os IDs estejam em ordem alfabética para um documentId consistente
            val sortedIds = listOf(user1Id, user2Id).sorted()
            val matchDocumentId = "${sortedIds[0]}_${sortedIds[1]}"

            val matchData = mapOf(
                "user1Id" to sortedIds[0],
                "user2Id" to sortedIds[1],
                "timestamp" to FieldValue.serverTimestamp()
            )

            // Usa set para criar ou sobrescrever o documento, garantindo que ele exista
            matchesCollection.document(matchDocumentId).set(matchData).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}