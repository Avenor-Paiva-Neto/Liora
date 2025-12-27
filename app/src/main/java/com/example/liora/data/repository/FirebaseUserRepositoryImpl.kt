package com.example.liora.data.repository

import com.example.liora.domain.model.UserProfile
import com.example.liora.domain.repository.UserRepository
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.Calendar
import java.util.Date

class FirebaseUserRepositoryImpl(
    private val firestore: FirebaseFirestore
) : UserRepository {

    private val usersCollection = firestore.collection("users")

    override suspend fun saveUserData(user: UserProfile): Result<Unit> {
        return try {
            usersCollection.document(user.uid).set(user.toMap()).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getUserData(userId: String): Result<UserProfile?> {
        return try {
            val documentSnapshot = usersCollection.document(userId).get().await()
            val userProfile = documentSnapshot.toObject(UserProfile::class.java)
            Result.success(userProfile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getPotentialCandidates(currentUserProfile: UserProfile): Result<List<UserProfile>> {
        return try {
            // Lógica original restaurada: Filtragem por idade e ordenação
            val currentYear = Calendar.getInstance().get(Calendar.YEAR)
            val minAge = currentUserProfile.preferences.minAge
            val maxAge = currentUserProfile.preferences.maxAge

            val latestBirthYear = currentYear - maxAge
            val earliestBirthYear = currentYear - minAge

            val query = usersCollection
                .whereNotEqualTo("uid", currentUserProfile.uid)
                .orderBy("birthYear")
                .whereGreaterThanOrEqualTo("birthYear", latestBirthYear)
                .whereLessThanOrEqualTo("birthYear", earliestBirthYear)
                .limit(150)

            val querySnapshot = query.get().await()
            val profiles = querySnapshot.toObjects(UserProfile::class.java)

            Result.success(profiles)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateUndoState(userId: String, undoStateUpdate: Map<String, Any?>): Result<Unit> {
        return try {
            // Lógica original restaurada: Suporte para FieldValue.delete()
            val firestoreUpdateMap = undoStateUpdate.entries.associate { (key, value) ->
                val firestoreValue = when (value) {
                    null -> FieldValue.delete()
                    else -> value
                }
                "undoState.$key" to firestoreValue
            }

            usersCollection.document(userId).update(firestoreUpdateMap).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * MÉTODO REMOVIDO: observeMatchedUsers
     * A responsabilidade de observar os matches foi movida para FirebaseInteractionRepositoryImpl.
     * A lógica anterior olhava para uma subcoleção incorreta.
     */
    // override fun observeMatchedUsers(currentUserId: String): Flow<List<UserProfile>> = callbackFlow { ... }

    /**
     * NOVO MÉTODO: Observa os perfis de usuários dado uma lista de IDs.
     *
     * Utiliza um snapshot listener para obter atualizações em tempo real para os perfis
     * especificados pelos userIds. É crucial que a lista de userIds tenha no máximo 10 elementos
     * para estar em conformidade com as limitações do operador 'whereIn' do Firestore em consultas em tempo real.
     * O Use Case que chama este método (ObserveMatchedUsersUseCase) será responsável por chunkar a lista de IDs.
     *
     * @param userIds Uma lista de IDs de usuários cujos perfis devem ser observados (máximo 10 por consulta).
     * @return Um [Flow] de uma lista de [UserProfile] correspondentes aos IDs fornecidos.
     */
    override fun observeUserProfilesByIds(userIds: List<String>): Flow<List<UserProfile>> = callbackFlow {
        // Se a lista de IDs estiver vazia, emite uma lista vazia e fecha o Flow.
        if (userIds.isEmpty()) {
            trySend(emptyList())
            awaitClose { /* Nada para fechar */ }
            return@callbackFlow
        }

        // Verifica o limite do Firestore para whereIn. Se o UseCase enviar mais de 10,
        // a query falhará no lado do Firebase SDK. Este é um ponto de controle crucial.
        if (userIds.size > 10) {
            // Em um ambiente de produção, você pode querer logar um erro ou lançar uma exceção mais específica aqui.
            // Por enquanto, a API do Firestore lançará uma exceção se a lista for muito grande.
            // Para garantir que o comportamento seja robusto, o UseCase deve garantir o chunking.
        }

        // Constrói a query para observar os perfis dos usuários com os UIDs fornecidos.
        val query = usersCollection.whereIn("uid", userIds)

        // Adiciona o snapshot listener para observar as mudanças em tempo real.
        val subscription = query.addSnapshotListener { snapshot, e ->
            if (e != null) {
                close(e) // Propaga o erro para o Flow
                return@addSnapshotListener
            }

            if (snapshot != null) {
                // Mapeia os documentos do snapshot para objetos UserProfile.
                val profiles = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(UserProfile::class.java)
                }
                trySend(profiles) // Envia a lista de perfis para o Flow
            } else {
                trySend(emptyList()) // Envia uma lista vazia se o snapshot for nulo (improvável com listener)
            }
        }

        // Garante que o listener seja removido quando o Flow for cancelado (ex: quando o ViewModel é limpo).
        awaitClose { subscription.remove() }
    }
}