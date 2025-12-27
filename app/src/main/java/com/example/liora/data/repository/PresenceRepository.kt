package com.example.liora.data.repository

import com.example.liora.domain.repository.PresenceRepository
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/**
 * Implementação concreta da interface [PresenceRepository] que interage com o Firebase Realtime Database.
 *
 * Esta classe é responsável por:
 * - Atualizar o status de presença (online/offline) de um usuário.
 * - Observar o status de presença de um usuário em tempo real.
 * - Atualizar o status de digitação de um usuário em uma conversa específica.
 * - Observar o status de digitação de um usuário em uma conversa específica em tempo real.
 *
 * O Realtime Database é particularmente adequado para dados de presença devido à sua
 * natureza de baixa latência e sincronização em tempo real.
 *
 * @param realtimeDatabase Instância do FirebaseDatabase injetada para comunicação com o banco de dados.
 */
class FirebasePresenceRepositoryImpl(
    private val realtimeDatabase: FirebaseDatabase
) : PresenceRepository {

    // Referências aos nós raiz no Realtime Database para organizar os dados
    private val usersRef = realtimeDatabase.getReference("users")
    private val typingStatusRef = realtimeDatabase.getReference("typingStatus")

    /**
     * Atualiza o status de presença de um usuário no Realtime Database.
     * Se o usuário ficar online, um `onDisconnect()` é configurado para automaticamente
     * definir o status como offline caso a conexão seja perdida.
     *
     * @param userId O ID do usuário cujo status de presença será atualizado.
     * @param isOnline `true` se o usuário estiver online, `false` caso contrário.
     * @return [Result]<Unit> Indicando sucesso ou falha da operação assíncrona.
     */
    override suspend fun updatePresence(userId: String, isOnline: Boolean): Result<Unit> {
        return try {
            val userStatusRef = usersRef.child(userId).child("online")
            if (isOnline) {
                userStatusRef.setValue(true).await()
                // Define para offline quando o usuário desconectar inesperadamente
                userStatusRef.onDisconnect().setValue(false)
            } else {
                userStatusRef.setValue(false).await()
                // Remove o onDisconnect se o usuário sair explicitamente
                userStatusRef.onDisconnect().cancel()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Fornece um fluxo reativo que emite o status de presença de um usuário específico em tempo real.
     *
     * @param userId O ID do usuário a ser observado.
     * @return [Flow]<Boolean> Onde `true` significa online e `false` significa offline.
     */
    override fun observePresence(userId: String): Flow<Boolean> = callbackFlow {
        val userStatusRef = usersRef.child(userId).child("online")
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // Obtém o valor booleano do snapshot, padrão para false se nulo
                val isOnline = snapshot.getValue(Boolean::class.java) ?: false
                trySend(isOnline) // Envia o status para o Flow
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException()) // Envia o erro para o Flow e fecha-o
            }
        }
        userStatusRef.addValueEventListener(listener) // Adiciona o listener
        awaitClose { userStatusRef.removeEventListener(listener) } // Remove o listener quando o Flow é cancelado
    }

    /**
     * Atualiza o status de digitação de um usuário dentro de uma conversa específica no Realtime Database.
     *
     * @param matchId O ID da conversa onde o status de digitação está sendo atualizado.
     * @param userId O ID do usuário que está digitando (ou parou de digitar).
     * @param isTyping `true` se o usuário estiver digitando, `false` caso contrário.
     * @return [Result]<Unit> Indicando sucesso ou falha da operação assíncrona.
     */
    override suspend fun updateTypingStatus(matchId: String, userId: String, isTyping: Boolean): Result<Unit> {
        return try {
            val typingRef = typingStatusRef.child(matchId).child(userId)
            if (isTyping) {
                typingRef.setValue(true).await()
                // Opcional: remover o status de digitação se o usuário desconectar ou a sessão expirar
                typingRef.onDisconnect().removeValue()
            } else {
                typingRef.setValue(false).await()
                // Cancela o onDisconnect se o usuário parar de digitar explicitamente
                typingRef.onDisconnect().cancel()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Fornece um fluxo reativo que emite o status de digitação de um usuário específico
     * dentro de uma conversa específica em tempo real.
     *
     * @param matchId O ID da conversa a ser observada.
     * @param otherUserId O ID do usuário cujo status de digitação será observado.
     * @return [Flow]<Boolean> Onde `true` significa digitando e `false` significa não digitando.
     */
    override fun observeTypingStatus(matchId: String, otherUserId: String): Flow<Boolean> = callbackFlow {
        val typingRef = typingStatusRef.child(matchId).child(otherUserId)
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // Obtém o valor booleano do snapshot, padrão para false se nulo
                val isTyping = snapshot.getValue(Boolean::class.java) ?: false
                trySend(isTyping) // Envia o status para o Flow
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException()) // Envia o erro para o Flow e fecha-o
            }
        }
        typingRef.addValueEventListener(listener) // Adiciona o listener
        awaitClose { typingRef.removeEventListener(listener) } // Remove o listener quando o Flow é cancelado
    }
}
